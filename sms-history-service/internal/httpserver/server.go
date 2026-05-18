package httpserver

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strings"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"

	"sms-history-service/internal/sms"
)

type Server struct {
	collection *mongo.Collection
}

func NewServer(collection *mongo.Collection) *Server {
	return &Server{collection: collection}
}

func (s *Server) Routes() *http.ServeMux {
	mux := http.NewServeMux()
	mux.HandleFunc("/v1/user/", s.getMessagesHandler)
	return mux
}

func (s *Server) getMessagesHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		w.Header().Set("Allow", http.MethodGet)
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	userID, ok := extractUserID(r.URL.Path)
	if !ok {
		http.NotFound(w, r)
		return
	}

	cursor, err := s.collection.Find(r.Context(), bson.M{"userId": userID})
	if err != nil {
		s.writeError(w, http.StatusInternalServerError, fmt.Errorf("failed to query messages: %w", err))
		return
	}
	defer cursor.Close(r.Context())

	var messages []sms.Record
	if err := cursor.All(r.Context(), &messages); err != nil {
		s.writeError(w, http.StatusInternalServerError, fmt.Errorf("failed to decode messages: %w", err))
		return
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(messages); err != nil {
		s.writeError(w, http.StatusInternalServerError, fmt.Errorf("failed to encode response: %w", err))
	}
}

func extractUserID(path string) (string, bool) {
	const prefix = "/v1/user/"
	const suffix = "/messages"

	if !strings.HasPrefix(path, prefix) || !strings.HasSuffix(path, suffix) {
		return "", false
	}

	userID := strings.TrimSuffix(strings.TrimPrefix(path, prefix), suffix)
	if userID == "" {
		return "", false
	}

	return userID, true
}

func (s *Server) writeError(w http.ResponseWriter, status int, err error) {
	http.Error(w, err.Error(), status)
}
