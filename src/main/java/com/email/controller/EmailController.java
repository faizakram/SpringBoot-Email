package com.email.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Template;
import freemarker.template.TemplateException;

@RestController
@RequestMapping("/email")
public class EmailController {
	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private FreeMarkerConfigurer freemarkerConfigurer;

	private static final String NOREPLY_ADDRESS = "noreply@baeldung.com";
	@Value("classpath:/mail-logo.png")
	private Resource resourceFile;

	@PostMapping("/sendMail")
	public ResponseEntity<?> sendEmail(@RequestParam String emailTo,
			@RequestParam String subject) {
		// @RequestParam String emailTo, @RequestParam String body,@RequestParam String
		// subject, @RequestParam MultipartFile file
		
		CompletableFuture.runAsync(() -> {
			try {
				sendMessageUsingFreemarkerTemplate(emailTo, subject, new HashMap<>());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// method call or code to be asynch.
		});
		return new ResponseEntity<String>("Success", HttpStatus.ACCEPTED);
	}

	/**
	 * 
	 * @param to
	 * @param subject
	 * @param templateModel
	 * @param file 
	 * @throws IOException
	 * @throws TemplateException
	 * @throws MessagingException
	 */
	public void sendMessageUsingFreemarkerTemplate(String to, String subject, Map<String, Object> templateModel)
			throws IOException, TemplateException, MessagingException {
		templateModel.put("recipientName", "Hello");
		templateModel.put("senderName", "World");
		templateModel.put("text", "World");
		Template freemarkerTemplate = freemarkerConfigurer.getConfiguration().getTemplate("template-freemarker.ftl");
		String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, templateModel);

		sendHtmlMessage(to, subject, htmlBody);
	}

	/**
	 * 
	 * @param to
	 * @param subject
	 * @param htmlBody
	 * @param file 
	 * @throws MessagingException
	 * @throws IOException 
	 */
	private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException, IOException {
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setFrom(NOREPLY_ADDRESS);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(htmlBody, true);
		helper.addInline("attachment.png", resourceFile);
		emailSender.send(message);
	}
}
