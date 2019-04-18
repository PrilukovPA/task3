package task3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Application;

/**
 * Класс реализует REST сервис с одним методом stat, 
 * на вызов которого отвечет именем и версией артефакта maven. 
 * @author Прилуков П.А.
 *
 */
public class Task3 {
	
	private Main camel;
	private static final String HOST = "localhost";
	private static final String PORT = "8080";	
	private static final String API_STAT = "stat";

	public static void main(String[] args) throws Exception {
		Task3 task = new Task3();
		task.execute();
	}
	
	public void execute() throws Exception {
		camel = new Main();
		camel.addRouteBuilder(new RestRoute(HOST, PORT, API_STAT));		
		System.out.println("Press Ctrl+C to terminate JVM\n");
        camel.run();
	}
	
	/**
	 * Класс реализует маршрут, предоставляющий REST-сервис, 
	 * который возвращает информацию Maven в формате JSON 
	 * при GET-запросе на указанный метод.
	 *
	 */
	private static class RestRoute extends RouteBuilder {
		
		private String host = null;
		private String port = null;
		private String api = null;
		
		public RestRoute(String host, String port, String api) {	
			this.host = host;
			this.port = port;
			this.api = api;
		}

		@Override
		public void configure() {
			
			restConfiguration()
				.component("restlet")
				.host(host)
				.port(port);
			
			MavenInfo info = new MavenInfo();
			
			rest(api)
				.get()
					.produces("application/json")
					.route()	
					.transform(constant(info))
					.marshal().json(JsonLibrary.Jackson);
		}		
	}
}

/**
 * Класс предназначен для получения имени и версии артефакта Maven.
 *
 */
class MavenInfo {
	private String name = null;
	private String version = null;
	
	public MavenInfo() {
		try {
			getPomInfo();
		} catch (IOException | XmlPullParserException e) {
			name = "unknown";
			version = "unknown";
		} 
	}
	
	private void getPomInfo() throws FileNotFoundException, IOException, XmlPullParserException {
		MavenXpp3Reader reader = new MavenXpp3Reader();
        File fpom = new File("pom.xml");
		Model model = null;
		if (fpom.exists()) {
          model = reader.read(new FileReader(fpom));
		} else {
			try (InputStream is = Application.class.getResourceAsStream("/META-INF/maven/ru.prilukov/task3/pom.xml");
				 InputStreamReader isr = new InputStreamReader(is))
			{
				model = reader.read(isr);
			}
		}
		name = model.getArtifactId();
		version = model.getVersion();
	}
	
	/**
	 * 
	 * @return имя артефакта Maven
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return версия артефакта Maven
	 */
	public String getVersion() {
		return version;
	}
}
