# SMS Monitoring App

## Overview
This Android application allows users to monitor SMS messages and maintain a connection with a server through regular ping requests. The app runs in the background even when the phone is locked.

## Features
- Enter phone numbers (supports multiple numbers with comma separation)
- View the last 10 requests made by the app
- Send ping requests to https://comm.com/ every 10 seconds
- Listen to incoming SMS messages and forward them to the server
- Run in the background even when the phone is locked

## Technical Details

### Ping Requests
The app sends a POST request to https://comm.com/ every 10 seconds with the following structure:
```json
{
  "type": "ping",
  "phoneNumber": "1234567890,123456788" // User-entered phone numbers
}
```

### SMS Forwarding
When an SMS is received, the app forwards it to https://comm.com/ with the following structure:
```json
{
  "type": "sms",
  "sender": "+351924874745",
  "recipient": "Unknown",
  "body": "yo",
  "pdu": "7,-111,35,48,16,33,0,104,36,12,-111,83,-111,66,120,116,84,0,1,82,48,113,17,116,-123,0,2,-7,55",
  "phoneNumber": "..." // User-entered phone numbers
}
```

## Permissions
The app requires the following permissions:
- RECEIVE_SMS: To receive SMS messages
- READ_SMS: To read SMS content
- INTERNET: To communicate with the server
- FOREGROUND_SERVICE: To run in the background
- POST_NOTIFICATIONS: To show a notification for the foreground service

## Setup
1. Enter the phone numbers in the text field (comma-separated for multiple numbers)
2. Click "Save" to store the phone numbers
3. The app will automatically start sending ping requests and listening for SMS messages
4. Use the floating action button to start/stop the service

## Request History
The app maintains a history of the last 10 requests (both ping and SMS) with details including:
- Request type (PING or SMS)
- Timestamp
- Phone numbers
- Request details
- Success/failure status
