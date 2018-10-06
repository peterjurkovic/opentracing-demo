package config

import (
	"os"
	"time"
)

var (

	ServerAddress = getenv("QUOTA_SERVER_ADDRESS", ":6060")

	ServiceName = "quota-service"

	SleepTimeout =  100 * time.Millisecond

	AgentHost = "localhost"

	AgentPort = 5778

	HttpCollectorURL =  getenv("QUOTA_HTTP_COLLECTOR_URL", "http://localhost:14268/api/traces")
)


func getenv(key, fallback string) string {
	value := os.Getenv(key)
	if len(value) == 0 {
		return fallback
	}
	return value
}