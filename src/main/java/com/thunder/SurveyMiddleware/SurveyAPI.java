package com.thunder.SurveyMiddleware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.google.gson.Gson;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("api")
public class SurveyAPI {
	private static Map<String, String> env = System.getenv();
	private static Map<String, Object> configOverrides = new HashMap<String, Object>(){{
		put("javax.persistence.jdbc.url", env.get("DB_URL"));
		put("javax.persistence.jdbc.user", env.get("DB_USER"));
		put("javax.persistence.jdbc.password", env.get("DB_PASSWORD"));
	}};
	
	private static EntityManagerFactory factory = Persistence.createEntityManagerFactory("surveys", configOverrides);
	private static EntityManager em = factory.createEntityManager();
	
	private static String generic500message = "Something went wrong with our servers, try again shortly";

	@POST
	@Path("/initialize")
	public Response initialize() {
		Survey s1 = new Survey("Pol", "Ajazi", "2676 Centennial Ct", "Alexandria", "Virginia", "22311", "2024898714",
				"pol.ajazi@yahoo.com", "1618461192994", new ArrayList<String>(Arrays.asList("students", "dorms")),
				"friends", "likely");
		Survey s2 = new Survey("Flavio", "Amurrio", "7710 Kalorama Dr", "Annandale", "Virginia",
				"22003", "2407762442", "famurrio@gmu.edu", "1618461192992",
				new ArrayList<String>(Arrays.asList("students")), "internet", "veryLikely");

		try {
			em.getTransaction().begin();
			em.persist(s1);
			em.persist(s2);
			em.getTransaction().commit();
			
			Session session = em.unwrap(Session.class);
			Query<Survey> query = session.createQuery("from survey");
			List<Survey> surveys = query.list();
			return Response.ok(surveys).build();
		} catch (Exception e) {
			em.clear();
			System.out.println((new Gson()).toJson(e));
			return Response.status(500).entity(getErrObj(e.getMessage())).build();
		}
	}
	
	
	@GET
	@Path("/survey/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSurvey(@PathParam("id") int id) {
		System.out.println("\n\nGET by id:"+id);
		try {
			Survey survey = em.find(Survey.class, id);
			if (survey != null)
				return Response.ok(survey).build();
			return Response.status(404).entity(getErrObj("Survey with id: " + id + " not found")).build();
		} catch ( Exception e) {
			em.clear();
			System.out.println((new Gson()).toJson(e));
			return Response.status(500).entity(getErrObj(generic500message)).build();
		}
	}

	@GET
	@Path("/surveys")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getySurveys() {
		System.out.println("\n\nGET all");
		try {
			Session session = em.unwrap(Session.class);
			List<Survey> surveys = session.createQuery("from survey").list();
			return Response.ok(surveys).build();
		} catch (Exception e) {
			System.out.println((new Gson()).toJson(e));
			return Response.status(500).entity(getErrObj(generic500message)).build();
		}
	}

	@POST
	@Path("/survey")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addSurvey(Survey survey) {
		System.out.println("\n\nPOST:");
		System.out.println((new Gson()).toJson(survey));
		try {
			HashMap<String, String> validationResults = ValidateSurvey.validate(survey, null);
			if(validationResults.size() != 0)
				return Response.status(400).entity(validationResults).build();
			
			em.getTransaction().begin();
			em.persist(survey);
			em.getTransaction().commit();
			
			return Response.ok(survey).build();
		} catch (Exception e) {
			return Response.status(500).entity(getErrObj(generic500message)).build();
		}
	}
	
	@PUT
	@Path("/survey/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSurvey(@PathParam("id") int id, Survey newSurvey) {
		System.out.println("\n\nPUT:");
		System.out.println((new Gson()).toJson(newSurvey));
		try {
			final Survey foundSurvey = em.find(Survey.class, id);
			if (foundSurvey == null)
				return Response.status(404).entity(getErrObj("Survey with id: " + id + " not found")).build();
			
			HashMap<String, String> originalPhoneAndEmail = new HashMap<String, String>() {{
			    put("phone", foundSurvey.getPhone());
			    put("email", foundSurvey.getEmail());
			}};
			 
			HashMap<String, String> validationResults = ValidateSurvey.validate(newSurvey, originalPhoneAndEmail);
			if(validationResults.size() != 0)
				return Response.status(400).entity(validationResults).build();
			
			foundSurvey.updateWithNewSurvey(newSurvey);
			
			em.getTransaction().begin();
			em.merge(foundSurvey);
			em.getTransaction().commit();

			return Response.ok(foundSurvey).build();
		} catch ( Exception e) {
			System.out.println((new Gson()).toJson(e));
			return Response.status(500).entity(getErrObj(generic500message)).build();
		}
	}

	private static HashMap<String, String> getErrObj(final String message) {
		return new HashMap<String, String>() {
			{
				put("error", message);
			}
		};
	}

}
