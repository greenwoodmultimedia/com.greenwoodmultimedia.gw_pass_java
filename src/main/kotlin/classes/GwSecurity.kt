package classes

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class GwSecurity(secretKey: SecretKey, vecteurInitialisation: ByteArray)
{
    companion object
    {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1"
        private const val LONGUEUR_CLE = 256
        private const val ITERATION_CLE = 1000
        private const val LONGUEUR_SEL = 16
        private const val LONGUEUR_VECTEUR_INITIALISATION = 16

        fun generateSecretKey(cle: CharArray, sel: ByteArray): SecretKey
        {
            val pbeKeySpec = PBEKeySpec(cle, sel, ITERATION_CLE, LONGUEUR_CLE)
            val sfk = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val midKey = sfk.generateSecret(pbeKeySpec);
            return SecretKeySpec(midKey.encoded, "AES")
        }

        fun genererSel(): ByteArray
        {
            val sel = ByteArray(LONGUEUR_SEL)
            val rd = Random()
            rd.nextBytes(sel)
            return sel
        }

        fun genererVecteurInitialisation(): ByteArray
        {
            val vecteurInitialisation = ByteArray(LONGUEUR_VECTEUR_INITIALISATION)
            val rd = Random()
            rd.nextBytes(vecteurInitialisation)
            return vecteurInitialisation
        }

        fun sha256(motDePasse:String): String
        {
            val messageDigested:ByteArray = MessageDigest.getInstance("SHA-256").digest(motDePasse.toByteArray())
            return messageDigested.fold("") { str, it -> str + "%02x".format(it) }
        }
    }

    private val secretKey:SecretKey
    private var vecteurInitialisation:ByteArray = ByteArray(16)

    init
    {
        try
        {
            this.vecteurInitialisation = vecteurInitialisation
            this.secretKey = secretKey
        }
        catch (ex: Exception)
        {
            throw IllegalArgumentException(ex)
        }
    }

    fun encrypter(value: String): String?
    {
        try
        {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val iv = IvParameterSpec(this.vecteurInitialisation)
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, iv)
            val encrypted = cipher.doFinal(value.toByteArray())
            return String(Base64.getEncoder().encode(encrypted))
        }
        catch (ex: Exception)
        {
            return null;
        }
    }

    fun decrypter(encrypted: String?): String?
    {
        try
        {
            val iv = IvParameterSpec(this.vecteurInitialisation)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, this.secretKey, iv)
            val original = cipher.doFinal(Base64.getDecoder().decode(encrypted))
            return String(original)
        }
        catch (ex: Exception)
        {
            GwConsole.ecrireConsole(ex.printStackTrace().toString())
            return null
        }
    }
}