package utils

/* 
 * Password Hashing With PBKDF2 (http://crackstation.net/hashing-security.htm).
 * Copyright (c) 2013, Taylor Hornby
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import java.math.BigInteger
import play.api.libs.Crypto
import play.api.Play
import java.lang.Integer
import models._

object CryptoHandler {
    val PBKDF2_ALGORITHM: String = "PBKDF2WithHmacSHA1"
    val CREATION_TOKEN: String = Crypto.sign(
        Play.current.configuration.getString("application.secret") match { 
            case Some(secret) => secret
       })

    val SALT_BYTE_SIZE = 24;
    val HASH_BYTE_SIZE = 24;
    val PBKDF2_ITERATIONS = 1000;

    val ITERATION_INDEX = 0;
    val SALT_INDEX = 1;
    val PBKDF2_INDEX = 2;

    def validateToken(token: String) = slowEquals(token.toCharArray, CREATION_TOKEN.toCharArray)

    def createHash(value: String) = {
        val random = new SecureRandom()
        var salt = new Array[Byte](SALT_BYTE_SIZE)
        random.nextBytes(salt)

        val hash = pbkdf2(value.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)

        s"${PBKDF2_ITERATIONS}:${toHex(salt)}:${toHex(hash)}"
    }

    def validateCacheToken(username: String, token: String) = validatePassword(username, token)

    def validatePassword(password: String, correctHash: String) = {
       	val splitHash = correctHash.split(":")
		val (iterations, salt, hash) = (
            Integer.parseInt(splitHash(ITERATION_INDEX)), 
            fromHex(splitHash(SALT_INDEX)), 
			fromHex(splitHash(PBKDF2_INDEX))
		)

        val testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length)
        slowEquals(hash, testHash)
    }

    private def slowEqualsRec(a: Array[Byte], b: Array[Byte]): Boolean = {
        def slowEquals(a: Array[Byte], b: Array[Byte], acc: Int): Int = {
            if(a.isEmpty || b.isEmpty) acc
            else slowEquals(a.tail, b.tail, acc | (a.head ^ b.head))
        }

        slowEquals(a, b, a.length ^ b.length) == 0
    }

    private def slowEquals(a: Array[Byte], b: Array[Byte]): Boolean = 
        (a zip b map { case (x, y) => x ^ y } foldLeft (a.length ^ b.length)) (_ | _) == 0

    private def slowEquals(a: Array[Char], b: Array[Char]): Boolean = 
        (a zip b map { case (x, y) => x ^ y } foldLeft (a.length ^ b.length)) (_ | _) == 0

    private def pbkdf2(password: Array[Char], salt: Array[Byte], iterations: Int, bytes: Int): Array[Byte] = {
        val spec = new PBEKeySpec(password, salt, iterations, bytes * 8)
        
        SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).getEncoded()
    }

    private def fromHex(hex: String): Array[Byte] = {
        for (i <- 0 until hex.length() / 2) 
        yield Integer.parseInt(hex.substring(2*i, 2*i + 2), 16).asInstanceOf[Byte]
    } toArray

    private def toHex(array: Array[Byte]): String = {
        val bi = new BigInteger(1, array)
        val hex = bi.toString(16)
        val paddingLength = (array.length * 2) - hex.length
        
        if(paddingLength > 0) s"%${paddingLength}d".format(0) + hex
        else hex
    }
}