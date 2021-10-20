package it.grational.http.request
import it.grational.http.response.Response
import it.grational.http.response.HttpResponse

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

		this.url.openConnection(this.proxy).with {
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

			this.parameters.headers.each { k, v ->
				setRequestProperty(k,v)
			}

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