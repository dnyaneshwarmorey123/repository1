package com.example.ssodemo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.opencsv.CSVWriter;

@Component
public class IbmRestClient {

	final static String url = "https://exemplify.verify.ibm.com//v1.0/endpoint/default/token";

	private static String tokenString;

	private static CSVWriter writer;

	@Scheduled(fixedRate = 10000) // fixedRate value in ms is a time interval.
	public static void main() throws IOException {

		RestTemplate template = new RestTemplate();

		HttpHeaders header = new HttpHeaders();

		header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();

		map.add("client_id", "b1515374-1483-458e-a987-4b285600a63d");
		map.add("client_secret", "V4USO1KzJO");
		map.add("grant_type", "client_credentials");
		map.add("scope", "openid");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(map, header);

		ResponseEntity<AuthorizationDetails> response = template.exchange(url, HttpMethod.POST, entity,
				AuthorizationDetails.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			tokenString = response.getBody().getAccess_token();
			System.out.println("# " + tokenString);
			find("https://exemplify.verify.ibm.com/v2.0/Users", tokenString);
			find("https://exemplify.verify.ibm.com/v2.0/Groups", tokenString);
 			
		}else {
			System.out.println("Something went wrong!!");
		}
	}

	public static void find(String url, String tokenString) throws IOException {

		RestTemplate template = new RestTemplate();
		HttpHeaders header = new HttpHeaders();
		header.set("Authorization", "bearer " + tokenString);
		ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, new HttpEntity(header), String.class);

		String data = response.getBody();
		System.out.println(response.getStatusCodeValue());

		writeCsv(data, url);

	}

	public static void writeCsv(String content, String url) throws IOException {
		try {
			if (url.contains("Users")) {
				writer = new CSVWriter(new FileWriter(
						"C:/Users" + "/Dnyaneshwar/Desktop/IBM Ecosystems" + "/RestResponses/csvUsers.csv"));
			} else if (url.contains("Groups")) {
				writer = new CSVWriter(new FileWriter(
						"C:/Users" + "/Dnyaneshwar/Desktop/IBM Ecosystems" + "/RestResponses/csvGroups.csv"));
			}

			String arr[] = content.split(",");

			
			writer.writeNext(arr);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
