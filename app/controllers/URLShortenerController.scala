package controllers

import java.net.{InetAddress, MalformedURLException, URL}
import java.security.MessageDigest
import java.util
import javax.inject._
import javax.xml.bind.DatatypeConverter

import akka.actor.ActorSystem
import io.swagger.annotations._
import play.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * This is just initial
  */
@Singleton
@Api
class URLShortenerController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)
                                      (implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val urls: util.Map[String, String] = new util.HashMap[String, String]
  val baseURL: String = s"http://${InetAddress.getLocalHost().getHostName()}:9000/"

  @ApiOperation(value = "Shorten URL in the POST body.", notes = "", httpMethod = "POST", response = classOf[String], consumes = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "Long URL", required = true, paramType = "body", dataType = "string", defaultValue = "http://www.wikipedia.org")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 405, message = "Validation exception"))
  )
  def shorten: Action[AnyContent] = Action.async {
    implicit request =>
      try {
        val url = request.body.asText.getOrElse("")
        new URL(url)
        getShortenedURL(url).map {
          msg => Ok(msg)
        }
      }
      catch {
        case invalidURL: MalformedURLException =>
          Future {
            BadRequest
          }
      }
  }

  @ApiOperation(value = "Return long URL", notes = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid URL supplied"),
    new ApiResponse(code = 405, message = "Validation exception"))
  )
  def decode(@ApiParam(value = "Short URL", required = true)
             url: String): Action[AnyContent] = Action.async {
    getFutureDecode(url).map { url =>
      url match {
        case "404" => NotFound(url)
        case _ => Ok(url)
      }
    }
  }

  def getShortenedURL(longUrl: String): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    val msdDigest = MessageDigest.getInstance("SHA-1")
    msdDigest.update(longUrl.getBytes("UTF-8"), 0, longUrl.length)
    val shortHash = (DatatypeConverter.printHexBinary(msdDigest.digest)).substring(0, 7)
    val shortened: String = baseURL + shortHash
    urls.put(shortHash, longUrl)
    Logger.debug("Long url = " + longUrl)
    Logger.debug("Shortened = " + shortened)

    promise.success(shortened)
    promise.future
  }

  def getFutureDecode(url: String): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    Logger.debug("URL for decode = " + url)
    Logger.debug("Decoded long url = " + urls.getOrDefault(url, "404"))

    promise.success(urls.getOrDefault(url, "404"))
    promise.future
  }
}
