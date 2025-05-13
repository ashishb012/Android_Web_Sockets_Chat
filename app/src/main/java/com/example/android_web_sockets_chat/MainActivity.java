package com.example.android_web_sockets_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WebSocketChat";

    private EditText editTextServerUrl;
    private Button buttonConnect;
    private TextView textViewStatus;
    private TextView textViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;

    private OkHttpClient client;
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        editTextServerUrl = findViewById(R.id.edit_text_server_url);
        buttonConnect = findViewById(R.id.button_connect);
        textViewStatus = findViewById(R.id.text_view_status);
        textViewMessages = findViewById(R.id.text_view_messages);
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSend = findViewById(R.id.button_send);

        // Make the messages TextView scrollable
        textViewMessages.setMovementMethod(new ScrollingMovementMethod());

        // Disable send button initially
        buttonSend.setEnabled(false);

        // Initialize OkHttpClient
        client = new OkHttpClient();

        // Set click listener for the connect button
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWebSocket();
            }
        });

        // Set click listener for the send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void connectWebSocket() {
        String serverUrl = editTextServerUrl.getText().toString().trim();
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build WebSocket request
        Request request = new Request.Builder().url(serverUrl).build();

        // Create WebSocket listener
        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                MainActivity.this.webSocket = webSocket; // Store the WebSocket instance
                Log.d(TAG, "WebSocket connected");
                runOnUiThread(() -> {
                    textViewStatus.setText("Status: Connected");
                    buttonSend.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.d(TAG, "Received message: " + text);
                runOnUiThread(() -> {
                    appendMessage("Received: " + text);
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                Log.d(TAG, "Received message (bytes): " + bytes.hex());
                // Handle binary messages if needed
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.d(TAG, "WebSocket closing: " + code + " / " + reason);
                // Optionally close the connection here
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d(TAG, "WebSocket closed: " + code + " / " + reason);
                runOnUiThread(() -> {
                    textViewStatus.setText("Status: Disconnected");
                    buttonSend.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Disconnected from server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.e(TAG, "WebSocket failure", t);
                runOnUiThread(() -> {
                    textViewStatus.setText("Status: Connection Error");
                    buttonSend.setEnabled(false);
                    appendMessage("Connection error: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                });
            }
        };

        // Start the WebSocket connection
        client.newWebSocket(request, webSocketListener);
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (webSocket != null) {
            webSocket.send(message); // Send the message
            appendMessage("Sent: " + message);
            editTextMessage.setText(""); // Clear the input field
        } else {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
        }
    }

    private void appendMessage(String message) {
        // Append message and ensure scrolling
        textViewMessages.append(message + "\n");
        // Scroll to the bottom
        final int scrollAmount = textViewMessages.getLayout().getLineTop(textViewMessages.getLineCount()) - textViewMessages.getHeight();
        if (scrollAmount > 0) {
            textViewMessages.scrollTo(0, scrollAmount);
        } else {
            textViewMessages.scrollTo(0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the WebSocket connection when the activity is destroyed
        if (webSocket != null) {
            webSocket.close(1000, "Activity closing");
        }
        // Cancel any ongoing OkHttp calls (good practice, though less critical for WebSockets)
        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }
}


// Update your websocket server ip address or your device ip address if using localhost in network-security-config.xml file

// In the app enter
// ws://ip-address:8080