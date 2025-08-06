import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.ArrayList;
/**
 * This class provides a function to add two large numbers
 * represented as strings, handling optional comma separators.
 */
public class Solution {
    /**
     * Adds two large numbers given as strings and returns their sum as a string.
     * The function handles numbers that are 100 digits or more.
     * It also correctly handles and adds comma separators if they are present in the input.
     *
     * @param num1 The first number as a string.
     * @param num2 The second number as a string.
     * @return The sum of the two numbers as a string.
     */
    public static String addLargeNumbers(String num1, String num2) {
        // --- Step 1: Handle Comma Separators and Pre-processing ---
        // Check if the input strings have commas.
        // This flag will be used later to decide if commas should be added to the output.
        boolean addCommas = num1.contains(",") || num2.contains(",");
        // Remove commas from the input strings to perform digit-by-digit addition.
        // The problem states that the numbers are represented as strings, so we can
        // perform string manipulation to clean the input.
        String cleanedNum1 = num1.replace(",", "");
        String cleanedNum2 = num2.replace(",", "");
        // --- Step 2: Core Addition Logic (Digit-by-Digit) ---
        // Use a StringBuilder to build the result. It's more efficient for
        // repeated character appending than using String concatenation.
        StringBuilder result = new StringBuilder();
        int i = cleanedNum1.length() - 1; // Pointer for the first number, starting from the last digit.
        int j = cleanedNum2.length() - 1; // Pointer for the second number.
        int carry = 0; // Initialize the carry for addition.
        // Loop from the last digit to the first, as we do in manual addition.
        // The loop continues as long as there are digits in either number or a carry remains.
        while (i >= 0 || j >= 0 || carry > 0) {
            // Get the current digit from each number.
            // If the pointer has gone past the beginning of the string, the digit is 0.
            int digit1 = (i >= 0) ? cleanedNum1.charAt(i) - '0' : 0;
            int digit2 = (j >= 0) ? cleanedNum2.charAt(j) - '0' : 0;
            // Calculate the sum of the digits and the carry.
            int sum = digit1 + digit2 + carry;
            // The digit for the result is the last digit of the sum.
            result.append(sum % 10);
            // The new carry is the quotient of the sum divided by 10.
            carry = sum / 10;
            // Move to the next digits (towards the beginning of the string).
            i--;
            j--;
        }
        // --- Step 3: Format the Output ---
        // The result is currently in reverse order, so we need to reverse it.
        // E.g., adding 99 + 1 results in "001" in the StringBuilder. Reversing it gives "100".
        result.reverse();
        // --- Step 4: Add Commas if Required ---
        // If the original input had commas, format the output with commas.
        if (addCommas) {
            return addCommasToNumber(result.toString());
        }
        return result.toString();
    }
    /**
     * Helper function to add commas to a number string every three digits.
     * @param number The number string without commas.
     * @return The formatted number string with commas.
     */
    private static String addCommasToNumber(String number) {
        StringBuilder formattedNumber = new StringBuilder(number);
        // We start from the end of the string and insert commas every 3 digits.
        for (int i = formattedNumber.length() - 3; i > 0; i -= 3) {
            formattedNumber.insert(i, ',');
        }
        return formattedNumber.toString();
    }
    /**
     * Main method for testing the function.
     * This section would not be part of the final code on Coderbyte,
     * but is useful for local testing.
     */
    public static void main(String[] args) {
        // Test cases from the problem description
        System.out.println("Test Case 1: " + addLargeNumbers("1,200", "1,500")); // Expected: 2,700
        // Additional test cases
        System.out.println("Test Case 2: " + addLargeNumbers("999", "1")); // Expected: 1000
        System.out.println("Test Case 3: " + addLargeNumbers("1234567890123456789", "9876543210987654321"));
        // Expected: 11111111101111111110
        System.out.println("Test Case 4: " + addLargeNumbers("999,999,999", "1"));
        // Expected: 1,000,000,000
        System.out.println("Test Case 5: " + addLargeNumbers("1,234,567,890", "9,876,543,210"));
        // Expected: 11,111,111,100
        System.out.println("Test Case 6: " + addLargeNumbers("100", "200")); // Expected: 300
    }
}

