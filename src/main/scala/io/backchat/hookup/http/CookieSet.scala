package io.backchat.hookup.http

import org.jboss.netty.handler.codec.http.{HttpHeaders, HttpRequest}
import org.jboss.netty.handler.codec.http.cookie.{Cookie, ServerCookieDecoder, ServerCookieEncoder}
import scala.collection.mutable
import scala.collection.JavaConverters._


/**
 * Adapt cookies of a Message to a mutable Set.  Requests use the Cookie header and
 * Responses use the Set-Cookie header.  If a cookie is added to the CookieSet, a
 * header is automatically added to the Message.  If a cookie is removed from the
 * CookieSet, a header is automatically removed from the message.
 *
 * Note: This is a Set, not a Map, because we assume the caller should choose the
 * cookie based on name, domain, path, and possibly other attributes.
 */
class CookieSet(message: Message) extends
  mutable.SetLike[Cookie, mutable.Set[Cookie]] {

  def seq = Set.empty ++ iterator

  private[this] var _isValid = true

  private[this] val cookieHeaderName =
    if (message.isRequest)
      HttpHeaders.Names.COOKIE
    else
      HttpHeaders.Names.SET_COOKIE

  private[this] val cookies: mutable.Set[CookieWrapper] = {
    val res = Option(message.headers.get(cookieHeaderName)) map { cookieHeader =>
      try {
        (ServerCookieDecoder.STRICT.decode(cookieHeader).asScala map { c => new CookieWrapper(c) }).toSet
      } catch {
        case e: IllegalArgumentException =>
          _isValid = false
          Set.empty[CookieWrapper]
      }
    }
    mutable.Set[CookieWrapper]() ++ res.getOrElse(mutable.Set.empty)
  }

  /** Check if there was a parse error.  Invalid cookies are ignored. */
  def isValid = _isValid

  def +=(cookie: Cookie) = {
    cookies += new CookieWrapper(cookie)
    rewriteCookieHeaders()
    this
  }

  def -=(cookie: Cookie) = {
    cookies -= new CookieWrapper(cookie)
    rewriteCookieHeaders()
    this
  }

  def contains(cookie: Cookie) =
    cookies.contains(new CookieWrapper(cookie))

  def iterator = cookies map { _.cookie } iterator

  def empty = mutable.Set[Cookie]()

  protected def rewriteCookieHeaders() {
    // Clear all cookies - there may be more than one with this name.
    message.headers.remove(cookieHeaderName)

    // Add cookies back again
    cookies foreach { cookie =>
      message.headers.add(cookieHeaderName, ServerCookieEncoder.STRICT.encode(cookie.cookie))
    }
  }

  // Wrap Cookie to handle broken equals()
  protected[http] class CookieWrapper(val cookie: Cookie) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case other: CookieWrapper =>
          cookie.name   == other.cookie.name &&
          cookie.path   == other.cookie.path &&
          cookie.domain == other.cookie.domain
        case _ =>
          throw new IllegalArgumentException // shouldn't happen
      }
    }

    override def hashCode() = cookie.hashCode
  }
}
