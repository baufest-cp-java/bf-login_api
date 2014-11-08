package controllers

import services._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import utils._

object LoginController extends Controller {

	def create = Action(parse.json) { implicit request =>
		(request.body).transform(LoginService.newLoginTransformer) map { login =>
			if(CryptoHandler.validateToken((login \ "token").as[String])) createLogin(login)
			else BadRequest("400")
		} getOrElse BadRequest("400")
	}

	private def createLogin(loginJson: JsObject): Result = {
		val username = (loginJson \ "username").as[String]
		val password = (loginJson \ "password").as[String]

		LoginService.insert(username, password) map { _ => 
			Ok("201")
		} getOrElse InternalServerError("500")
	}

	def authenticate = Action(parse.json) { implicit request =>
		(request.body).transform(LoginService.loginTransformer).asOpt map { loginJson =>
			processLogin(loginJson)
		} getOrElse BadRequest("400")
	}

	private def processLogin(login: JsObject)(implicit request: RequestHeader): Result = {
		val username = (login \ "username").as[String]
		val password = (login \ "password").as[String]
		val tokenOpt = LoginService.getTokenFromDB(username, password)
			
		tokenOpt map { token =>
			Ok.withCookies(Cookie(s"login.${username}", token))
		} getOrElse Forbidden("403")
	}

	def logout = Action { implicit request =>
		NotImplemented("logout")
	}
}