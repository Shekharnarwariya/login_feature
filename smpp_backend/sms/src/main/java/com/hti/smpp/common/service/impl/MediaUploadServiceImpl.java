package com.hti.smpp.common.service.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.service.MediaUploadService;
import com.hti.smpp.common.util.IConstants;

@Service
public class MediaUploadServiceImpl implements MediaUploadService {
	private Logger logger = org.slf4j.LoggerFactory.getLogger(MediaUploadServiceImpl.class);
	private static Set<String> imageSet = new java.util.HashSet<String>();
	private static Set<String> audioSet = new java.util.HashSet<String>();
	private static Set<String> videoSet = new java.util.HashSet<String>();
	static {
		imageSet.add("jpg");
		imageSet.add("jpeg");
		imageSet.add("gif");
		imageSet.add("png");
		imageSet.add("bmp");
		imageSet.add("webp");
		imageSet.add("tiff");
		imageSet.add("psd");
		imageSet.add("raw");
		imageSet.add("heif");
		imageSet.add("indd");
		videoSet.add("mp4");
		videoSet.add("mov");
		videoSet.add("wmv");
		videoSet.add("flv");
		videoSet.add("f4v");
		videoSet.add("swf");
		videoSet.add("mkv");
		videoSet.add("webm");
		audioSet.add("mp3");
		audioSet.add("flac");
		audioSet.add("m4a");
		audioSet.add("wav");
		audioSet.add("wma");
		audioSet.add("aac");
	}

	@Override
	public ResponseEntity<?> UploadMedia(String title, List<String> link_urls, List<MultipartFile> items) {
		System.out.println("call time" + LocalTime.now());
		System.out.println("<--MediaUploadServiceImpl Called-->");
		String filename = "";
		String returnUrl = "";
		List<String> listUrl = new ArrayList();
		;
		try {
			MultipartFile item = null;
			String link_url = null;
			Iterator i = items.iterator();
			Iterator url = link_urls.iterator();
			while (i.hasNext() && url.hasNext()) {
				link_url = (String) url.next();
				item = (MultipartFile) i.next();
				System.out.println("Name: " + item.getOriginalFilename());
				System.out.println("ContentType: " + item.getContentType());
				filename = item.getOriginalFilename();
				String ext = filename.substring(filename.lastIndexOf(".") + 1);
				// String fileType = "other";
				filename = new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date())
						+ (new Random().nextInt(9999 - 1000) + 1000);
				if (IConstants.mediaupload.equalsIgnoreCase("ftp")) {
					String foldername = IConstants.FTPUser.substring(0, IConstants.FTPUser.indexOf("@"));
					System.out.println("Connecting to FTP Server");
					FTPClient ftpClient = new FTPClient();
					ftpClient.connect(IConstants.FTPServer);
					if (ftpClient.login(IConstants.FTPUser, IConstants.FTPPassword)) {
						ftpClient.enterLocalPassiveMode();
						ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
						System.out.println("Storing File to FTP Server");
						if (ftpClient.storeFile(filename + "." + ext, item.getInputStream())) {
							if (imageSet.contains(ext.toLowerCase())) {
								// create thumbnail
								ftpClient.storeFile(filename + "_thumb.png", item.getInputStream());
								// create html
								String html_text = "";
								if (link_url != null && link_url.length() > 0) {
									html_text = createImageHtmlContent(
											"https://" + IConstants.FTPServer + "/" + foldername + "/" + filename, ext,
											link_url, title);
								} else {
									html_text = createHtmlContent(
											"https://" + IConstants.FTPServer + "/" + foldername + "/" + filename, ext,
											"image", title);
								}
								InputStream targetStream = new ByteArrayInputStream(html_text.getBytes());
								ftpClient.storeFile(filename + ".html", targetStream);
								returnUrl = "https://" + IConstants.FTPServer + "/" + foldername + "/" + filename
										+ ".html";
							} else if (audioSet.contains(ext.toLowerCase())) {
								// copy file to local dir
								FileUtils.copyInputStreamToFile(item.getInputStream(),
										new File(IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "." + ext));
								// create thumbnail
								Icon icon = FileSystemView.getFileSystemView().getSystemIcon(
										new File(IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "." + ext));
								Image image = ((ImageIcon) icon).getImage();
								BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(),
										icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
								Graphics2D bufImageGraphics = bufferedImage.createGraphics();
								bufImageGraphics.drawImage(image, 0, 0, null);
								ImageIO.write(bufferedImage, "png",
										new File(IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "_thumb.png"));
								ftpClient.storeFile(filename + "_thumb.png", new java.io.FileInputStream(
										IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "_thumb.png"));
								// create html
								String html_text = createHtmlContent(
										"https://" + IConstants.FTPServer + "/" + foldername + "/" + filename, ext,
										"audio", title);
								InputStream targetStream = new ByteArrayInputStream(html_text.getBytes());
								ftpClient.storeFile(filename + ".html", targetStream);
								returnUrl = "https://" + IConstants.FTPServer + "/" + foldername + "/" + filename
										+ ".html";
							} else if (videoSet.contains(ext.toLowerCase())) {
								// copy file to local dir
								FileUtils.copyInputStreamToFile(item.getInputStream(),
										new File(IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "." + ext));
								// create thumbnail
								FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(
										IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "." + ext);
								frameGrabber.start();
								Java2DFrameConverter aa = new Java2DFrameConverter();
								try {
									BufferedImage bi;
									// for (int i = 0; i < 1000; i++) {
									Frame f = frameGrabber.grabKeyFrame();
									bi = aa.convert(f);
									while (bi != null) {
										ImageIO.write(bi, "png", new File(
												IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "_thumb.png"));
										f = frameGrabber.grabKeyFrame();
										bi = aa.convert(f);
									}
									frameGrabber.stop();
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									frameGrabber.close();
								}
								ftpClient.storeFile(filename + "_thumb.png", new java.io.FileInputStream(
										IConstants.WEBSMPP_EXT_DIR + "media//" + filename + "_thumb.png"));
								// create html
								String html_text = createHtmlContent(
										"https://" + IConstants.FTPServer + "/" + foldername + "/" + filename, ext,
										"video", title);
								InputStream targetStream = new ByteArrayInputStream(html_text.getBytes());
								ftpClient.storeFile(filename + ".html", targetStream);
								returnUrl = "https://" + IConstants.FTPServer + "/" + foldername + "/" + filename
										+ ".html";
							} else {
								returnUrl = "https://" + IConstants.FTPServer + "/" + foldername + "/" + filename + "."
										+ ext;
							}
							System.out.println("<-- File Uploaded -->");
						} else {
							returnUrl = "upload error";
							System.out.println("<---Unable to Upload File --> ");
						}
					} else {
						returnUrl = "ftp login error";
						System.out.println("<--- FTP Login Error --> ");
					}
				} else {
					if (!ext.equalsIgnoreCase("jsp")) {
						Path filePath = Paths.get(IConstants.WEBSMPP_EXT_DIR, "media", filename);
						item.transferTo(filePath);
						returnUrl = IConstants.WebUrl + "/media/" + filename;
						;
					} else {
						returnUrl = "upload error";
						System.out.println("<---Unable to Upload File --> ");
					}
				}
				System.out.println("url" + returnUrl);
				String shortUrl = getShortUrl(returnUrl);
				listUrl.add(shortUrl);
			}
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
			throw new InternalServerException(e.getMessage());
		}
		System.out.println("out put  time" + LocalTime.now());
		return ResponseEntity.ok(listUrl);
	}

	private String createImageHtmlContent(String url, String ext, String link_url, String title) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<html><head>");
		stringBuilder.append("<meta name='viewport' content='width=device-width, minimum-scale=0.1'>");
		stringBuilder.append("<meta property='og:title' content='" + title + "'/>");
		stringBuilder.append("<meta property='og:image' content='" + url + "_thumb.png'/>");
		stringBuilder.append("<meta property='og:url' content='" + url + ".html'/>");
		stringBuilder.append("<title>");
		stringBuilder.append(title);
		stringBuilder.append("</title>");
		stringBuilder.append("<style> .content {max-width: 1024px; margin: auto;} </style>");
		stringBuilder.append("</head>");
		stringBuilder.append("<body>");
		stringBuilder.append("<div class='content'>");
		stringBuilder.append("<a href='" + link_url + "'>");
		stringBuilder.append("<img style='width: 100%;' ");
		stringBuilder.append("src='" + url + "." + ext + "'>");
		stringBuilder.append("</a>");
		stringBuilder.append("</div>");
		stringBuilder.append("</body></html>");
		return stringBuilder.toString();
	}

	private String createHtmlContent(String url, String ext, String filetype, String title) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<html><head>");
		stringBuilder.append("<meta name='viewport' content='width=device-width, minimum-scale=0.1'>");
		stringBuilder.append("<meta property='og:title' content='" + title + "'/>");
		stringBuilder.append("<meta property='og:image' content='" + url + "_thumb.png'/>");
		stringBuilder.append("<meta property='og:url' content='" + url + ".html'/>");
		stringBuilder.append("<title>");
		stringBuilder.append(title);
		stringBuilder.append("</title>");
		stringBuilder.append("<style> .content {max-width: 1024px; margin: auto;} </style>");
		stringBuilder.append("</head>");
		stringBuilder.append("<body>");
		if (filetype.equals("image")) {
			stringBuilder.append("<div class='content'>");
			stringBuilder.append("<img style='width: 100%;' ");
			stringBuilder.append("src='" + url + "." + ext + "'>");
			stringBuilder.append("</div>");
		} else if (filetype.equals("audio")) {
			stringBuilder.append("<div align='center'>");
			stringBuilder.append("<audio controls autoplay>");
			stringBuilder.append("<source src='" + url + "." + ext + "'>");
			stringBuilder.append("</audio>");
			stringBuilder.append("</div>");
		} else if (filetype.equals("video")) {
			stringBuilder.append("<div align='center'>");
			stringBuilder.append("<video controls autoplay>");
			stringBuilder.append("<source src='" + url + "." + ext + "'>");
			stringBuilder.append("</video>");
			stringBuilder.append("</div>");
		} else {
			stringBuilder.append("<div align='center'>");
			stringBuilder.append("<iframe src='" + url + "." + ext
					+ "' style='width:100%; height:100%;' frameborder='0' allowfullscreen></iframe>");
			stringBuilder.append("</div>");
		}
		stringBuilder.append("</body></html>");
		return stringBuilder.toString();
	}

	public static String getShortUrl(String url) {
		System.out.println("short url call   time" + LocalTime.now());
		String accessToken = "9b23160c57745c45f1e9e96a66e6cd5fc0f3bb07";
		String apiUrl = "https://api-ssl.bitly.com/v4/shorten";
		String shortenedUrl = null;
		try {
			URL bitlyUrl = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) bitlyUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + accessToken);

			String jsonInputString = "{\"long_url\":\"" + url + "\"}";
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_CREATED) {
				try (Scanner scanner = new Scanner(connection.getInputStream())) {
					StringBuilder response = new StringBuilder();
					while (scanner.hasNextLine()) {
						response.append(scanner.nextLine());
					}

					// Extract the shortened URL from the response
					shortenedUrl = response.toString().split("\"link\":\"")[1].split("\"")[0];
					System.out.println("Short URL: " + shortenedUrl);
				}
			} else {
				// Handle error response
				System.out.println("Error: " + responseCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerException(e.getMessage());
		}
		System.out.println("sort url out put    time" + LocalTime.now());
		return shortenedUrl;
	}

}
