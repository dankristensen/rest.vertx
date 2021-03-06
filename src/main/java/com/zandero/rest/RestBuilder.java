package com.zandero.rest;

import com.zandero.rest.context.ContextProvider;
import com.zandero.rest.exception.ExceptionHandler;
import com.zandero.rest.reader.ValueReader;
import com.zandero.rest.writer.HttpResponseWriter;
import com.zandero.utils.Assert;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Helper class to build up RestRouter with all writers, readers, handlers and context providers in one place
 */
public class RestBuilder {

	private final Vertx vertx;

	private final Router router;

	private List<Object> apis = new ArrayList<>();

	private List<Class<? extends ExceptionHandler>> exceptionHandlers = new ArrayList<>();

	private Map<MediaType, Class<? extends HttpResponseWriter>> mediaTypeResponseWriters = new LinkedHashMap<>();
	private Map<Class<?>, Class<? extends HttpResponseWriter>> classResponseWriters = new LinkedHashMap<>();

	private Map<MediaType, Class<? extends ValueReader>> mediaTypeValueReaders = new LinkedHashMap<>();
	private Map<Class<?>, Class<? extends ValueReader>> classValueReaders = new LinkedHashMap<>();

	private Map<Class, ContextProvider> contextProviders = new LinkedHashMap<>();

	public RestBuilder(Router router) {

		Assert.notNull(router, "Missing vertx router!");

		this.router = router;
		this.vertx = null;
	}

	public RestBuilder(Vertx vertx) { // hide

		Assert.notNull(vertx, "Missing vertx!");

		this.router = null;
		this.vertx = vertx;
	}

	public RestBuilder register(Object... restApi) {

		Assert.notNullOrEmpty(restApi, "Missing REST API(s)!");

		apis.addAll(Arrays.asList(restApi));
		return this;
	}

	@SafeVarargs
	public final RestBuilder errorHandler(Class<? extends ExceptionHandler>... handlers) {

		Assert.notNullOrEmpty(handlers, "Missing exception handler(s)!");

		exceptionHandlers.addAll(Arrays.asList(handlers));
		return this;
	}

	public RestBuilder writer(Class<?> clazz, Class<? extends HttpResponseWriter> writer) {

		Assert.notNull(clazz, "Missing response class!");
		Assert.notNull(writer, "Missing response writer type class!");

		classResponseWriters.put(clazz, writer);
		return this;
	}

	public RestBuilder writer(String mediaType, Class<? extends HttpResponseWriter> writer) {

		Assert.notNullOrEmptyTrimmed(mediaType, "Missing media type!");
		Assert.notNull(writer, "Missing response writer class!");

		MediaType type = MediaType.valueOf(mediaType);
		Assert.notNull(type, "Unknown media type given: " + mediaType);

		mediaTypeResponseWriters.put(type, writer);
		return this;
	}

	public RestBuilder writer(MediaType mediaType, Class<? extends HttpResponseWriter> writer) {

		Assert.notNull(mediaType, "Missing media type!");
		Assert.notNull(writer, "Missing response writer class!");

		mediaTypeResponseWriters.put(mediaType, writer);
		return this;
	}

	public RestBuilder reader(Class<?> clazz, Class<? extends ValueReader> reader) {

		Assert.notNull(clazz, "Missing read in class!");
		Assert.notNull(reader, "Missing request reader type class!");

		classValueReaders.put(clazz, reader);
		return this;
	}

	public RestBuilder reader(String mediaType, Class<? extends ValueReader> reader) {

		Assert.notNullOrEmptyTrimmed(mediaType, "Missing media type!");
		Assert.notNull(reader, "Missing value reader class!");

		MediaType type = MediaType.valueOf(mediaType);
		Assert.notNull(type, "Unknown media type given: " + mediaType);

		mediaTypeValueReaders.put(type, reader);
		return this;
	}

	public RestBuilder reader(MediaType mediaType, Class<? extends ValueReader> reader) {

		Assert.notNull(mediaType, "Missing media type!");
		Assert.notNull(reader, "Missing value reader class!");

		mediaTypeValueReaders.put(mediaType, reader);
		return this;
	}

	public <T> RestBuilder context(Class<T> clazz, ContextProvider<T> provider) {

		Assert.notNull(clazz, "Missing provider class type!");
		Assert.notNull(provider, "Missing context provider!");

		contextProviders.put(clazz, provider);
		return this;
	}

	private Router getRouter() {
		if (vertx == null) {
			return RestRouter.register(router, apis);
		}

		return RestRouter.register(vertx, apis);
	}

	public Router build() {

		Assert.notNullOrEmpty(apis, "No REST API given, register at least one!");

		Router output = getRouter();

		// register APIs
		apis.forEach(api -> RestRouter.register(output, api));

		// register readers
		if (classValueReaders.size() > 0) {
			classValueReaders.forEach((clazz, reader) -> RestRouter.getReaders().register(clazz, reader));
		}
		if (mediaTypeValueReaders.size() > 0) {
			mediaTypeValueReaders.forEach((type, reader) -> RestRouter.getReaders().register(type, reader));
		}

		// register writers
		if (classResponseWriters.size() > 0) {
			classResponseWriters.forEach((clazz, reader) -> RestRouter.getWriters().register(clazz, reader));
		}
		if (mediaTypeResponseWriters.size() > 0) {
			mediaTypeResponseWriters.forEach((type, reader) -> RestRouter.getWriters().register(type, reader));
		}

		// register exception handlers
		if (exceptionHandlers.size() > 0) {
			exceptionHandlers.forEach(handler -> RestRouter.getExceptionHandlers().register(handler));
		}

		// register context providers
		if (contextProviders.size() > 0) {
			contextProviders.forEach((clazz, provider) -> RestRouter.getContextProviders().register(clazz, provider));
		}

		return output;
	}
}
