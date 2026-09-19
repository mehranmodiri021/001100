package com.example.billing.security

import android.util.Base64
import android.util.Log
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

object SecurityHelper {
    private const val TAG = "SecurityHelper"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    fun verifyPurchase(base64PublicKey: String?, signedData: String?, signature: String?): Boolean {
        if (base64PublicKey.isNullOrBlank()) {
            Log.e(TAG, "Purchase verification failed: base64PublicKey is null or blank.")
            return false
        }
        if (signedData.isNullOrBlank()) {
            Log.e(TAG, "Purchase verification failed: signedData is null or blank.")
            return false
        }
        if (signature.isNullOrBlank()) {
            Log.e(TAG, "Purchase verification failed: signature is null or blank.")
            return false
        }

        return try {
            val publicKey = generatePublicKey(base64PublicKey)
            verify(publicKey, signedData, signature)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during purchase verification: ${e.message}", e)
            false
        }
    }

    private fun generatePublicKey(encodedPublicKey: String): PublicKey {
        val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
    }

    private fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        return try {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray(Charsets.UTF_8))
            val signatureBytes = Base64.decode(signature, Base64.DEFAULT)
            sig.verify(signatureBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification exception: ${e.message}", e)
            false
        }
    }
}
