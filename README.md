# Call Limiter

Call Limiter is an Android application designed to help users set a time limit for phone calls to specific contacts. This app ensures calls do not exceed the defined duration, making it easier to manage call times effectively.

## Features

- **Call Duration Management**: Limits outgoing calls to the specified duration.
- **Phone Number Validation**: Ensures that only valid phone numbers can be used.
- **Persistent Storage**: Saves call limits using key-value pairs in SharedPreferences. 
- **Delete Option**: Allows users to remove the time limit for a specific phone number.
- **Bottom Sheet Timer**: A user-friendly scrollable wheel to select time limits in hours and minutes.

## How It Works

1. **Enter a Phone Number**: Manually input a valid number or select from contacts.
2. **Set a Time Limit**: Choose a duration using the bottom sheet timer.
3. **Save the Limit**: The app stores the number and its corresponding time limit.
4. **Monitor Calls**: Calls to the saved number will be restricted based on the set time.
5. **Delete a Limit**: Users can remove the time restriction for a number anytime.

This app is ideal for managing call durations effectively, whether for personal use or controlling excessive call times.
