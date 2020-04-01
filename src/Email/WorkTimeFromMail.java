
package Email;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
/**
 *
 * @author alpha
 */
public class WorkTimeFromMail {
     private static final String DATUM = "Datum: (\\d?\\d)\\.(\\d?\\d).(\\d\\d\\d\\d)";
    private static final String ARBEITSENDE = "Arbeitsende: (\\d?\\d):(\\d\\d)";
    private static final String ARBEITSDAUER = "Arbeitsdauer: (\\d?\\d):(\\d\\d)";
    private static final String PAUSE = "Pausendauer: (\\d?\\d):(\\d\\d)";
    private static final String GESAMTARBEITSDAUER = "Gesamtarbeitsdauer: (\\d?\\d):(\\d\\d)";
    private static final String ARBEITSBEGINN = "Arbeitsbeginn: (\\d?\\d):(\\d\\d)";
    private static final String ADRESSE = "\\s*(.*?)\\s+([\\w.-]+@[\\w.-]+)";
    private static Message message;
    private static String messgText;
    private static Pattern pattern;
    
    
     public static void main(String[] args) throws MessagingException, IOException {

        String host = "Outlook.office365.com"; 
        String mailStoreType = "pop3";
        String username = "h.dalkilic@outlook.de";
        String password = "h1322251509"; 
        int port = 995;
        String messageText = receiveEmail(host, mailStoreType, username, password, port);   
        extractInfo(messageText);
      

    }

    public static String receiveEmail(String pop3Host, String storeType,
            final String user, final String password, int port) throws MessagingException, IOException {
        try {

            //1) get the session object  
            Properties props = new Properties();
            props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.pop3.socketFactory.fallback", "false");
            props.put("mail.pop3.socketFactory.port", port);
            props.put("mail.pop3.port", port);
            props.put("mail.pop3.user", user);
            props.put("mail.store.protocol", storeType);
            props.put("mail.pop3.host", pop3Host);
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
            Session emailSession = Session.getDefaultInstance(props, auth);

            //2) create the POP3 store object and connect with the pop server  
            Store store = emailSession.getStore(storeType);
            store.connect(pop3Host, user, password);

            //3) create the folder object and open it  
            Folder emailFolder = store.getFolder("INBOX");

            emailFolder.open(Folder.READ_ONLY);

            //4) retrieve the messages from the folder in an array and print it  
            Message[] messages = emailFolder.getMessages();
            for (Message m : messages) {
                message = m;
//                System.out.println("---------------------------------");
//                System.out.println("Email Number " + (i + 1));
//                System.out.println("Subject: " + message.getSubject());
//                System.out.println("From: " + message.getFrom()[0]);
//                System.out.println("Text: " + message.getContent());
                //messages[i].writeTo(System.out);
                
            }
            messgText = getTextFromMessage(message);

            //5) close the store and folder objects  
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
        }
        return messgText;
    }

   

    public static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    public static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    public static void extractInfo(String message) {
        final org.jsoup.nodes.Document document = Jsoup.parse(message);       
        ArrayList<String> regex= new ArrayList<String>();
        regex.add(DATUM);
        regex.add(ARBEITSBEGINN);
        regex.add(ARBEITSENDE);
        regex.add(PAUSE);
        regex.add(ARBEITSDAUER);
        regex.add(GESAMTARBEITSDAUER);
        
        for (String i : regex) {
            pattern= Pattern.compile(i);
       Matcher m = pattern.matcher(document.text());
        while (m.find()) {
            System.out.println(m.group() + " ");
        } 
        }
         
        
    }
    
}
