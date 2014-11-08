import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class LoginControllerSpec extends PlaySpecification {

  "LoginController" should {

    "send 400 on a request without username" in new WithApplication {
      val jsonWithoutUsername = Json.obj("password" -> "test")
      val requestWithoutUsername = FakeRequest("POST", "/login", null, jsonWithoutUsername)
      val result = controllers.LoginController.authenticate(requestWithoutUsername)
      status(result) must equalTo(BAD_REQUEST)
    }

    "send 400 on a request without password" in new WithApplication {
      val jsonWithoutPassword = Json.obj("username" -> "test")
      val requestWithoutPassword = FakeRequest("POST", "/login", null, jsonWithoutPassword)
      val result = controllers.LoginController.authenticate(requestWithoutPassword)
      status(result) must equalTo(BAD_REQUEST)
    }

    "send 403 on a request with invalid username" in new WithApplication {
      val jsonWithInvalidUsername = Json.obj("username" -> "test", "password" -> "dotero")
      val requestWithInvalidUsername = FakeRequest("POST", "/login", null, jsonWithInvalidUsername)
      val result = controllers.LoginController.authenticate(requestWithInvalidUsername)
      status(result) must equalTo(FORBIDDEN)
    }

    "send 403 on a request with invalid password" in new WithApplication {
      val jsonWithInvalidPassword = Json.obj("username" -> "dotero", "password" -> "test")
      val requestWithInvalidPassword = FakeRequest("POST", "/login", null, jsonWithInvalidPassword)
      val result = controllers.LoginController.authenticate(requestWithInvalidPassword)
      status(result) must equalTo(FORBIDDEN)
    }

    "send 200 and add logged cookie on a request with valid username and password" in new WithApplication {
      val jsonWithLoginInformation = Json.obj("username" -> "dotero", "password" -> "dotero")
      val request = FakeRequest("POST", "/login", null, jsonWithLoginInformation)
      val result = controllers.LoginController.authenticate(request)
      status(result) must equalTo(OK)
      cookies(result).get("login.dotero") mustNotEqual None
    }

  }
}
