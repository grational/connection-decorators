package it.grational.http.request
import it.grational.http.response.Response
import it.grational.http.response.HttpResponse
import it.grational.proxy.NoProxy
import it.grational.proxy.EnvVar
import it.grational.proxy.EnvProxy
import it.grational.http.header.Authorization

/**
 * StandardRequest
 * This class is not instantiable since it requires some members to be defined.
 * The subclasses StandardGet/Head/Delete/Post/Put define what is needed and they
 * are, therefore, instantiable.
 */
abstract class StandardRequest implements HttpRequest {

	protected String    method
	protected URL       url
	protected String    body
	protected Map       parameters
	protected Proxy     proxy

	@Override
	HttpResponse connect() {
		Response result

		this.url.openConnection(proxyFromEnvironment()).with {
			requestMethod = this.method

			if (this.parameters.connectTimeout)
				setConnectTimeout ( // milliseconds
					this.parameters.connectTimeout
				)
			if (this.parameters.readTimeout)
				setReadTimeout ( // milliseconds
					this.parameters.readTimeout
				)
			if (this.parameters.allowUserInteraction)
				setAllowUserInteraction ( // boolean
					this.parameters.allowUserInteraction
				)
			if (this.parameters.useCaches)
				setUseCaches ( // boolean
					this.parameters.useCaches
				)
			if (!this.parameters.headers)
				this.parameters.headers = [:]

			if (this.url.userInfo)
				this.parameters.headers << this.addBasicAuth(this.url.userInfo)

			this.parameters.headers.each { k, v ->
				setRequestProperty(k,v)
			}

			if (this.parameters.cookies)
				setRequestProperty('Cookie',assembleCookies(this.parameters.cookies))

			if (this.body) {
				doOutput = true
				outputStream.withWriter { writer ->
					writer.write(this.body)
				}
			} else {
				connect()
			}

			result = new Response (
				code: responseCode,
				stream: inputStream
			)
		}
		return result
	}

	private Proxy proxyFromEnvironment() {
		Proxy result

		if (this.proxy)
			result = this.proxy
		else if ( new NoProxy().exclude(this.url) )
			result = Proxy.NO_PROXY
		else
			result = new EnvProxy (
				EnvVar.byURL(this.url).value(),
			).proxy()

		return result
	}

	protected Map addBasicAuth(String userInfo) {
		def (user,pass) = userInfo.tokenize(':')
		def auth = new Authorization (
			username: user,
			password: pass
		)
		return [ (auth.name()): auth.value() ]
	}

	protected String assembleCookies(Map cookies) {
		cookies.collect { k, v -> "${k}=${v};" }.join(' ')
	}

	@Override
	String toString() {
		String r
		r  = "method: ${this.method}"
		r += "\nurl: ${this.url}"
		if (this.body)
			r += "\nbody: ${this.body}"
		if (this.parameters)
			r += "\nparameters: ${this.parameters}"
		r += "\nproxy: ${this.proxy}"
		return r
	}
}
