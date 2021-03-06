package com.zandero.rest.test;

import com.zandero.rest.annotation.CatchWith;
import com.zandero.rest.exception.GenericExceptionHandler;
import com.zandero.rest.exception.WebApplicationExceptionHandler;
import com.zandero.rest.test.handler.IllegalArgumentExceptionHandler;
import com.zandero.rest.test.handler.JsonExceptionHandler;
import com.zandero.rest.test.handler.MyExceptionHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("/throw")
@CatchWith(JsonExceptionHandler.class) // catch globally for whole root
public class ErrorThrowingRest {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("ouch")
	public String returnOuch() {

		throw new IllegalArgumentException("Ouch!");
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("bang")
	@CatchWith(GenericExceptionHandler.class)
	public String returnBang() {

		throw new IllegalArgumentException("Bang!");
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("multi/{bang}")
	@CatchWith({IllegalArgumentExceptionHandler.class, WebApplicationExceptionHandler.class, MyExceptionHandler.class})
	public String returnMultiBang(@PathParam("bang") String bang) {

		switch (bang) {
			case "one":
				throw new NotAllowedException("Not for you!");

			case "two":
			default:
				throw new IllegalArgumentException("Bang!");

			case "three":
				throw new NumberFormatException("WHAT!");

			case "four":
				throw new AbstractMethodError("ADIOS!");
		}
	}

}
