package com.example.billing.security

import android.util.Base64
import android.util.Log
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

/**
 * SecurityHelper provides cryptographic verification of Cafe Bazaar in-app billing purchases
 * using RSA public-key cryptography and SHA1withRSA algorithm.
 *
 * Strict fail-closed policy:
 * Any missing or invalid key, empty data/signature, or exception MUST return false.
 */
object SecurityHelper {
    private const val TAG = "SecurityHelper"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    /**
     * Verifies that the data was signed with the given signature using the Base64-encoded public key.
     *
     * @param base64PublicKey The Base64-encoded RSA public key from Cafe Bazaar console.
     * @param signedData The JSON string containing purchase data.
     * @param signature The Base64-encoded signature returned from Bazaar.
     * @return true ONLY if verification succeeds mathematically, false otherwise.
     */
    fun verifyPurchase(base64PublicKey: String?, signedData: String?, signature: String?): Boolean {
        if (base64PublicKey.isNullOrBlank()) {
            Log.e(TAG, "Verification failed: Public key is missing or blank (fail-closed)")
            return false
        }
        if (signedData.isNullOrBlank()) {
            Log.e(TAG, "Verification failed: Signed data is null or blank")
            return false
        }
        if (signature.isNullOrBlank()) {
            Log.e(TAG, "Verification failed: Signature is null or blank")
            return false
        }

        return try {
            val publicKey = generatePublicKey(base64PublicKey.trim())
            if (publicKey == null) {
                Log.e(TAG, "Verification failed: Unable to generate RSA PublicKey from Base64")
                return false
            }
            verify(publicKey, signedData.trim(), signature.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception during verification: ${e.message}", e)
            false
        }
    }

    /**
     * Generates a PublicKey instance from a Base64-encoded string.
     */
    fun generatePublicKey(encodedPublicKey: String): PublicKey? {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "RSA algorithm not supported: ${e.message}", e)
            null
        } catch (e: InvalidKeySpecException) {
            Log.e(TAG, "Invalid key specification: ${e.message}", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Base64 decode failed for public key: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode public key: ${e.message}", e)
            null
        }
    }

    /**
     * Verifies that the signature matches the signed data using the provided PublicKey.
     */
    fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        return try {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray(Charsets.UTF_8))
            val signatureBytes = Base64.decode(signature, Base64.DEFAULT)
            if (!sig.verify(signatureBytes)) {
                Log.e(TAG, "Signature verification failed: signature does not match signed data")
                false
            } else {
                Log.d(TAG, "Cryptographic verification successful for purchase data")
                true
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "NoSuchAlgorithmException: $SIGNATURE_ALGORITHM", e)
            false
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "InvalidKeyException: Invalid public key", e)
            false
        } catch (e: SignatureException) {
            Log.e(TAG, "SignatureException during verification: ${e.message}", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Base64 decode failed for signature: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Verification error: ${e.message}", e)
            false
        }
    }
}
