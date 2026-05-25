package com.ucassignments.securesoftdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.io.FileReader;

/*@SpringBootApplication
@EnableCaching*/


public class SecuredApp {

	public static void main(String[] args) throws Exception {

		new SecuredApp();


	}

	public SecuredApp () {
		Bar b = new Bar();
		Bar b1 = new Bar();
		update(b);
		update(b1);
		b1 = b;
		update(b);
		update(b1);
	}


	private void update(Bar b) {
		b.x = 20;
		System.out.println(b.x);
	}

	private class Bar {
		int x = 10;
	}
}
