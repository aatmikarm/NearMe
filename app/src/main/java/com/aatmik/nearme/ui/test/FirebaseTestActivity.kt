package com.aatmik.nearme.ui.test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aatmik.nearme.databinding.ActivityFirebaseTestBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.UUID
import java.net.InetAddress
import kotlin.concurrent.thread

class FirebaseTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirebaseTestBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseTestActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity creation started")

        try {
            // Enable verbose Firebase logging
            FirebaseFirestore.setLoggingEnabled(true)
            //Log.d(TAG, "Firebase SDK Version: ${FirebaseFirestore.getSdkVersion()}")

            binding = ActivityFirebaseTestBinding.inflate(layoutInflater)
            Log.d(TAG, "onCreate: View binding initialized successfully")

            setContentView(binding.root)
            Log.d(TAG, "onCreate: Content view set with the inflated layout")

            // Check if Firestore instance is initialized
            if (firestore != null) {
                Log.d(TAG, "onCreate: Firestore instance initialized successfully")

                // Add connection state listener
                firestore.addSnapshotsInSyncListener {
                    Log.d(TAG, "Firestore snapshots in sync - connection is working")
                }

                // Enable network connectivity explicitly
                firestore.enableNetwork().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Firestore network connection enabled successfully")
                    } else {
                        Log.e(TAG, "Failed to enable Firestore network connection", task.exception)
                    }
                }
            } else {
                Log.e(TAG, "onCreate: Failed to initialize Firestore instance")
            }

            // Test network connectivity to Firestore in a background thread
            thread {
                try {
                    Log.d(TAG, "Starting network connectivity test to Firestore servers")
                    val isConnected = InetAddress.getByName("firestore.googleapis.com").isReachable(5000)
                    Log.d(TAG, "Can reach Firestore servers: $isConnected")
                } catch (e: Exception) {
                    Log.e(TAG, "Network connectivity test failed", e)
                }
            }

            setupOrderByDropdown()
            setupListeners()
            Log.d(TAG, "onCreate: Activity creation completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Exception occurred during activity creation", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        Log.d(TAG, "setupListeners: Setting up button click listeners")

        try {
            // Save button
            binding.btnSave.setOnClickListener {
                Log.d(TAG, "Save button clicked")
                saveUserToFirestore()
            }
            Log.d(TAG, "setupListeners: Save button listener set successfully")

            // Retrieve button
            binding.btnRetrieve.setOnClickListener {
                Log.d(TAG, "Retrieve button clicked")
                retrieveUsersWithOptions()
            }
            Log.d(TAG, "setupListeners: Retrieve button listener set successfully")

            Log.d(TAG, "setupListeners: All listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "setupListeners: Exception occurred while setting up listeners", e)
        }
    }

    private fun saveUserToFirestore() {
        Log.d(TAG, "saveUserToFirestore: Starting user save operation")

        try {
            // Update UI to show loading state
            binding.progressBar.visibility = android.view.View.VISIBLE
            Log.d(TAG, "saveUserToFirestore: Progress bar visibility set to VISIBLE")

            binding.tvStatus.text = "Saving user to Firestore..."
            Log.d(TAG, "saveUserToFirestore: Status text updated to 'Saving user to Firestore...'")

            // Create dummy user data
            val userId = UUID.randomUUID().toString()
            Log.d(TAG, "saveUserToFirestore: Generated UUID: $userId")

            val user = hashMapOf(
                "name" to "Test User",
                "email" to "test@example.com",
                "age" to 25,
                "createdAt" to System.currentTimeMillis()
            )
            Log.d(TAG, "saveUserToFirestore: Created user data: $user")

            // Log Firestore collection and document path
            Log.d(TAG, "saveUserToFirestore: Attempting to save to collection 'test_users', document ID: $userId")

            // Create a timeout handler
            val timeoutHandler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                Log.e(TAG, "saveUserToFirestore: Operation timed out after 15 seconds")
                binding.progressBar.visibility = android.view.View.GONE
                binding.tvStatus.text = "Error: Operation timed out. Check network connection and Firebase setup."
                Toast.makeText(this, "Operation timed out", Toast.LENGTH_LONG).show()
            }

            // Set a timeout for the operation
            timeoutHandler.postDelayed(timeoutRunnable, 15000) // 15 seconds timeout

            // Save to Firestore with network status check
            Log.d(TAG, "saveUserToFirestore: Testing network state before save operation")
            firestore.collection("test_users").document("network_test").delete()
                .addOnSuccessListener {
                    Log.d(TAG, "saveUserToFirestore: Network is available, proceeding with save operation")

                    // Save to Firestore
                    firestore.collection("test_users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            // Remove the timeout handler since operation succeeded
                            timeoutHandler.removeCallbacks(timeoutRunnable)

                            Log.d(TAG, "saveUserToFirestore: Document successfully written to Firestore")
                            Log.d(TAG, "saveUserToFirestore: Collection: test_users, Document ID: $userId")

                            binding.progressBar.visibility = android.view.View.GONE
                            Log.d(TAG, "saveUserToFirestore: Progress bar visibility set to GONE")

                            binding.tvStatus.text = "User saved successfully! ID: $userId"
                            Log.d(TAG, "saveUserToFirestore: Status text updated to success message")

                            binding.tvUserId.text = "User ID: $userId"
                            Log.d(TAG, "saveUserToFirestore: User ID text updated")

                            Toast.makeText(this, "User saved successfully!", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "saveUserToFirestore: Success toast displayed")

                            // Store the ID for retrieval
                            binding.etUserId.setText(userId)
                            Log.d(TAG, "saveUserToFirestore: User ID set in input field for easy retrieval")
                        }
                        .addOnFailureListener { e ->
                            // Remove the timeout handler since operation failed with an error
                            timeoutHandler.removeCallbacks(timeoutRunnable)

                            Log.e(TAG, "saveUserToFirestore: Error writing document to Firestore", e)
                            Log.e(TAG, "saveUserToFirestore: Error details: ${e.message}")

                            binding.progressBar.visibility = android.view.View.GONE
                            Log.d(TAG, "saveUserToFirestore: Progress bar visibility set to GONE after error")

                            binding.tvStatus.text = "Error saving user: ${e.message}"
                            Log.d(TAG, "saveUserToFirestore: Status text updated to error message")

                            Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "saveUserToFirestore: Error toast displayed")
                        }
                }
                .addOnFailureListener { e ->
                    // Remove the timeout handler since network test failed
                    timeoutHandler.removeCallbacks(timeoutRunnable)

                    Log.e(TAG, "saveUserToFirestore: Network test failed", e)
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.tvStatus.text = "Network error: ${e.message}"
                    Toast.makeText(this, "Network error: Check your connection", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "saveUserToFirestore: Unexpected exception occurred", e)
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvStatus.text = "Error: ${e.message}"
            Toast.makeText(this, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Enhanced function to retrieve users from the database with flexible options
     * @param userIds List of user IDs to retrieve. If empty, retrieves all users.
     * @param limit Maximum number of users to retrieve (for pagination). Ignored if userIds is not empty.
     * @param orderBy Field to order results by
     * @param ascending Whether to order results in ascending order
     * @param onSuccess Callback function when retrieval is successful
     * @param onFailure Callback function when retrieval fails
     */
    private fun retrieveUsers(
        userIds: List<String> = emptyList(),
        limit: Int = 50,
        orderBy: String = "createdAt",
        ascending: Boolean = false,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG, "retrieveUsers: Starting user retrieval with options")
        Log.d(TAG, "retrieveUsers: User IDs to retrieve: ${if (userIds.isEmpty()) "ALL" else userIds}")
        Log.d(TAG, "retrieveUsers: Limit: $limit, OrderBy: $orderBy, Ascending: $ascending")

        try {
            // Set up timeout
            val timeoutHandler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                Log.e(TAG, "retrieveUsers: Operation timed out after 15 seconds")
                onFailure(Exception("Operation timed out"))
            }
            timeoutHandler.postDelayed(timeoutRunnable, 15000) // 15 seconds timeout

            if (userIds.isEmpty()) {
                // Retrieve all users with ordering and limit
                Log.d(TAG, "retrieveUsers: Retrieving all users with limit $limit")

                var query = firestore.collection("test_users")
                    .orderBy(orderBy, if (ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                query.get()
                    .addOnSuccessListener { result ->
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        processQueryResults(result, onSuccess)
                    }
                    .addOnFailureListener { e ->
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        Log.e(TAG, "retrieveUsers: Error retrieving all users", e)
                        onFailure(e)
                    }
            } else if (userIds.size == 1) {
                // Retrieve a single user
                Log.d(TAG, "retrieveUsers: Retrieving single user with ID: ${userIds[0]}")

                firestore.collection("test_users")
                    .document(userIds[0])
                    .get()
                    .addOnSuccessListener { document ->
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        if (document != null && document.exists()) {
                            Log.d(TAG, "retrieveUsers: Single user retrieved successfully")
                            val userData = document.data
                            if (userData != null) {
                                // Add the id field to the map
                                val userMap = userData.toMutableMap()
                                userMap["id"] = document.id
                                onSuccess(listOf(userMap))
                            } else {
                                onSuccess(emptyList())
                            }
                        } else {
                            Log.w(TAG, "retrieveUsers: No user found with ID: ${userIds[0]}")
                            onSuccess(emptyList())
                        }
                    }
                    .addOnFailureListener { e ->
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        Log.e(TAG, "retrieveUsers: Error retrieving single user", e)
                        onFailure(e)
                    }
            } else {
                // Retrieve multiple users by ID (batched for efficiency)
                Log.d(TAG, "retrieveUsers: Retrieving ${userIds.size} specific users")

                // For small batches, we can use a simple approach
                if (userIds.size <= 10) {
                    retrieveUserBatch(userIds, timeoutHandler, timeoutRunnable, onSuccess, onFailure)
                } else {
                    // For larger batches, we need to split the requests due to Firestore limitations
                    Log.d(TAG, "retrieveUsers: Splitting large batch into smaller batches")
                    val batches = userIds.chunked(10)
                    val allResults = mutableListOf<Map<String, Any>>()
                    var completedBatches = 0
                    var error: Exception? = null

                    for (batch in batches) {
                        retrieveUserBatch(batch, timeoutHandler, timeoutRunnable,
                            onSuccess = { results ->
                                synchronized(allResults) {
                                    allResults.addAll(results)
                                    completedBatches++

                                    // Check if all batches are complete
                                    if (completedBatches == batches.size) {
                                        if (error == null) {
                                            Log.d(TAG, "retrieveUsers: All batches complete, total users: ${allResults.size}")
                                            onSuccess(allResults)
                                        } else {
                                            // An error occurred in at least one batch
                                            onFailure(error!!)
                                        }
                                    }
                                }
                            },
                            onFailure = { e ->
                                synchronized(allResults) {
                                    error = e
                                    completedBatches++

                                    // Check if all batches are complete
                                    if (completedBatches == batches.size) {
                                        onFailure(error!!)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "retrieveUsers: Unexpected exception occurred", e)
            onFailure(e)
        }
    }

    /**
     * Helper function to retrieve a batch of users by ID
     */
    private fun retrieveUserBatch(
        userIds: List<String>,
        timeoutHandler: Handler,
        timeoutRunnable: Runnable,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG, "retrieveUserBatch: Retrieving batch of ${userIds.size} users")

        firestore.collection("test_users")
            .whereIn("__name__", userIds)
            .get()
            .addOnSuccessListener { results ->
                timeoutHandler.removeCallbacks(timeoutRunnable)
                processQueryResults(results, onSuccess)
            }
            .addOnFailureListener { e ->
                timeoutHandler.removeCallbacks(timeoutRunnable)
                Log.e(TAG, "retrieveUserBatch: Error retrieving batch", e)
                onFailure(e)
            }
    }

    /**
     * Process query results into a standardized format
     */
    private fun processQueryResults(results: QuerySnapshot, onSuccess: (List<Map<String, Any>>) -> Unit) {
        Log.d(TAG, "processQueryResults: Processing ${results.size()} results")

        val userList = mutableListOf<Map<String, Any>>()

        for (document in results) {
            Log.d(TAG, "processQueryResults: Processing document ${document.id}")
            val userData = document.data.toMutableMap()
            // Add the ID field to the map
            userData["id"] = document.id
            userList.add(userData)
        }

        Log.d(TAG, "processQueryResults: Processed ${userList.size} users")
        onSuccess(userList)
    }

    /**
     * Enhanced UI method to retrieve and display users with options from the UI
     */
    private fun retrieveUsersWithOptions() {
        Log.d(TAG, "retrieveUsersWithOptions: Starting retrieval with UI options")

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = "Retrieving users from Firestore..."
        binding.tvResult.text = ""

        try {
            // Get user IDs from input field (comma-separated)
            val userIdInput = binding.etUserId.text.toString().trim()
            val userIds = if (userIdInput.isEmpty()) {
                emptyList()
            } else {
                userIdInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }

            // Get limit from input (default 50)
            val limitStr = binding.etLimit.text.toString()
            val limit = if (limitStr.isEmpty()) 50 else limitStr.toIntOrNull() ?: 50

            // Get order by field
            val orderBy = binding.dropdownOrderBy.text.toString().takeIf { it.isNotEmpty() } ?: "createdAt"

            // Get sort order
            val ascending = binding.cbAscending.isChecked

            Log.d(TAG, "retrieveUsersWithOptions: Params - UserIDs: $userIds, Limit: $limit, OrderBy: $orderBy, Ascending: $ascending")

            // Call the flexible retrieve function
            retrieveUsers(
                userIds = userIds,
                limit = limit,
                orderBy = orderBy,
                ascending = ascending,
                onSuccess = { users ->
                    binding.progressBar.visibility = android.view.View.GONE

                    if (users.isNotEmpty()) {
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("Users (${users.size}):\n\n")

                        for (user in users) {
                            stringBuilder.append("User ID: ${user["id"]}\n")
                            stringBuilder.append("Name: ${user["name"]}\n")
                            stringBuilder.append("Email: ${user["email"]}\n")
                            stringBuilder.append("Age: ${user["age"]}\n")
                            stringBuilder.append("Created: ${user["createdAt"]}\n\n")
                        }

                        binding.tvResult.text = stringBuilder.toString()
                        binding.tvStatus.text = "Retrieved ${users.size} users"
                        Log.d(TAG, "retrieveUsersWithOptions: Successfully displayed ${users.size} users")
                    } else {
                        binding.tvStatus.text = if (userIds.isEmpty()) "No users found in database" else "No users found with specified ID(s)"
                        binding.tvResult.text = "No data found"
                        Log.d(TAG, "retrieveUsersWithOptions: No users found")
                    }
                },
                onFailure = { e ->
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.tvStatus.text = "Error retrieving users: ${e.message}"
                    Log.e(TAG, "retrieveUsersWithOptions: Error", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "retrieveUsersWithOptions: Exception in UI handling", e)
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvStatus.text = "Error: ${e.message}"
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Initialize the order by dropdown with field options
     */
    private fun setupOrderByDropdown() {
        val fields = arrayOf("createdAt", "name", "age", "email")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fields)
        binding.dropdownOrderBy.setAdapter(adapter)
        binding.dropdownOrderBy.setText("createdAt", false)
        Log.d(TAG, "setupOrderByDropdown: Order by dropdown initialized with field options")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity started")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity resumed, becoming visible to user")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity paused, partially visible but losing focus")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Activity stopped, no longer visible to user")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity being destroyed")
    }
}