package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/segmentio/kafka-go"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"

	"sms-history-service/internal/httpserver"
	"sms-history-service/internal/sms"
)

const (
	mongoURI       = "mongodb://localhost:27017"
	databaseName   = "smsdb"
	collectionName = "sms_history"
	kafkaBroker    = "localhost:9092"
	kafkaTopic     = "sms-events"
	kafkaGroupID   = "go-sms-group"
	httpAddr       = ":8081"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	client, err := mongo.Connect(ctx, options.Client().ApplyURI(mongoURI))
	if err != nil {
		log.Fatalf("failed to connect to MongoDB: %v", err)
	}
	defer func() {
		if err := client.Disconnect(context.Background()); err != nil {
			log.Printf("failed to disconnect MongoDB: %v", err)
		}
	}()

	collection := client.Database(databaseName).Collection(collectionName)

	httpServer := httpserver.NewServer(collection)
	srv := &http.Server{
		Addr:         httpAddr,
		Handler:      httpServer.Routes(),
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		log.Printf("HTTP server listening on %s", httpAddr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("http server error: %v", err)
		}
	}()

	go consumeKafka(ctx, collection)

	<-ctx.Done()
	log.Println("shutdown signal received")

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Printf("http server shutdown error: %v", err)
	}
}

func consumeKafka(ctx context.Context, collection *mongo.Collection) {
	reader := kafka.NewReader(kafka.ReaderConfig{
		Brokers: []string{kafkaBroker},
		Topic:   kafkaTopic,
		GroupID: kafkaGroupID,
	})
	defer reader.Close()

	log.Println("Kafka consumer started")

	for {
		select {
		case <-ctx.Done():
			log.Println("Kafka consumer stopping")
			return
		default:
		}

		message, err := reader.ReadMessage(ctx)
		if err != nil {
			log.Printf("error reading Kafka message: %v", err)
			time.Sleep(time.Second)
			continue
		}

		var record sms.Record
		if err := json.Unmarshal(message.Value, &record); err != nil {
			log.Printf("failed to unmarshal sms event: %v", err)
			continue
		}

		record.EnsureCreatedAt()

		if _, err := collection.InsertOne(ctx, record); err != nil {
			log.Printf("failed to insert sms record: %v", err)
			continue
		}

		log.Printf("sms record saved for user %s", record.UserID)
	}
}
