package sms

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type Record struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"id,omitempty"`
	UserID      string             `bson:"userId" json:"userId"`
	PhoneNumber string             `bson:"phoneNumber" json:"phoneNumber"`
	Message     string             `bson:"message" json:"message"`
	Status      string             `bson:"status" json:"status"`
	Reason      string             `bson:"reason" json:"reason"`
	CreatedAt   time.Time          `bson:"createdAt,omitempty" json:"createdAt,omitempty"`
}

func (r *Record) EnsureCreatedAt() {
	if r.CreatedAt.IsZero() {
		r.CreatedAt = time.Now().UTC()
	}
}
