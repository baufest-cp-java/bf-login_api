package services

import anorm._ 
import anorm.SqlParser._
import play.api.cache.Cache
import play.api.db.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.Play.current
import utils.CryptoHandler

object LoginService {
	val loginTransformer = 
		(__ \ 'username).json.pickBranch and
		(__ \ 'password).json.pickBranch reduce

	val newLoginTransformer = 
		(__ \ 'username).json.pickBranch and
		(__ \ 'password).json.pickBranch and 
		(__ \ 'token).json.pickBranch reduce

	def getTokenFromDB(username: String, password: String): Option[String] = DB.withConnection("login") { implicit connection =>
		val query = "SELECT password FROM login WHERE username = {username}"

		for {
			dbPassword <- SQL(query).on('username -> username).as(scalar[String].singleOpt)
			if CryptoHandler.validatePassword(password, dbPassword)
		} yield createCacheToken(username, dbPassword)
	}

	private def createCacheToken(username: String, password: String) = CryptoHandler.createHash(username)

	def insert(username: String, password: String): Option[Long] = DB.withConnection("login") { implicit connection =>
		SQL("insert into login (username, password) values ({username}, {password})")
			.on('username -> username, 'password -> CryptoHandler.createHash(password))
			.executeInsert()
	}
}