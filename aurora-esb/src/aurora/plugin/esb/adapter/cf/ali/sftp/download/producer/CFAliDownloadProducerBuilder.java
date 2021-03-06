package aurora.plugin.esb.adapter.cf.ali.sftp.download.producer;

import java.util.logging.Level;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RouteDefinition;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import aurora.plugin.esb.AuroraEsbContext;
import aurora.plugin.esb.console.ConsoleLog;
import aurora.plugin.esb.model.Producer;

public class CFAliDownloadProducerBuilder extends RouteBuilder {

	private ConsoleLog clog = new ConsoleLog();
	private AuroraEsbContext esbContext;
	private Producer producer;
	private CompositeMap producerMap;

	public CFAliDownloadProducerBuilder(AuroraEsbContext esbContext,
			Producer producer) {
		this.esbContext = esbContext;
		this.producer = producer;
	}

	public CFAliDownloadProducerBuilder(AuroraEsbContext esbContext,
			CompositeMap producer) {
		this.esbContext = esbContext;
		this.producerMap = producer;
		
//		JndiRegistry
	}

	@Override
	public void configure() throws Exception {

		String downloadUrl = "sftp://115.124.16.69:22/" + "download";
		String orgCode = "CFCar";
		String downloadPara = "?username=cfcar&password=123456&delay=100s"
				+ "&noop=true" + "&recursive=true";

		String local_save_path = "file:/Users/shiliyan/Desktop/esb/download";
		String save_para = "";

		CompositeMap config = producerMap.getChild("sftp");
		downloadUrl = config.getString("downloadUrl".toLowerCase(), "");
		orgCode = config.getString("orgCode".toLowerCase(), "");
		// downloadPara = config.getString("downloadPara", "");
		downloadPara = config.getChild("downloadPara") == null ? "" : config
				.getChild("downloadPara").getText();

		String ftp_server_url = downloadUrl + "/" + orgCode
				+ downloadPara.trim();

		config = producerMap.getChild("local");
		local_save_path = config.getString("localSavePath".toLowerCase(), "");
		orgCode = config.getString("orgCode".toLowerCase(), "");
		// save_para = config.getString("savePara", "");
		save_para = config.getChild("savePara") == null ? "" : config.getChild(
				"savePara").getText();

		// + "?charset=utf-8"
		String local_url = local_save_path + "/" + orgCode + save_para.trim();

		// + "&charset=utf-8"
		// #idempotent=true
		//
		// # for the server we want to delay 5 seconds between polling the
		// server
		// # and move downloaded files to a done sub directory

		// lets shutdown faster in case of in-flight messages stack up
		getContext().getShutdownStrategy().setTimeout(10);

		RouteDefinition from = from(ftp_server_url);
		from.setCustomId(true);
		from.setId("cf.ali.car.ftp.download");
		from.to(local_url).bean(new LogBean(esbContext), "log")
				.log("Downloaded file ${file:name} complete.");

		esbContext.getmLogger().log(Level.SEVERE,
				"" + "[Downloaded File] " + "DOWNLOAD Task Configed");
		esbContext.getmLogger().log(Level.SEVERE,
				"" + "[Downloaded File] " + "DOWNLOAD URL " + ftp_server_url);
		esbContext.getmLogger().log(Level.SEVERE,
				"" + "[Downloaded File] " + "SAVE URL " + local_url);
		clog.log2Console("[Downloaded File] " + "DOWNLOAD Task Configed");
		clog.log2Console("[Downloaded File] " + "DOWNLOAD URL "
				+ ftp_server_url);
		clog.log2Console("[Downloaded File] " + "SAVE URL " + local_url);
		
		
		from("timer://foo?period=600000").process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {

				CamelContext context = exchange.getContext();
				// context.getRoute("abc.efg");
				context.stopRoute("cf.ali.car.ftp.download");
				context.startRoute("cf.ali.car.ftp.download");
				clog.log2Console("[Downloaded File] " + "DOWNLOAD Task Configed...");
			}
		});

	}
}
