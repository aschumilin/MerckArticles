import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class HtmlCleaner {


	public static void main(String[] args) throws IOException {
		/*
		 * first remove \r\n tokens from original documents: sed -i 's/\\r\\n//g' ./*
		 * extract these elements only:
		 * - title
		 * - h1, h2, h3
		 * - div class="para" (and all nested p elements)
		 * - div class="table-box"
		 * enclose in <!DOCTYPE html> <html lang="en-US">
		 */
//		String testPath = "/home/pilatus/Dropbox/AIFB/12_nach_Master/Merck_IBM-Watson/Cardiovascular_Examination-Cardiovascular-Disorders_original.html";

		
		String targetDirPath = "/home/pilatus/WORK/DATA/Merck_IBM-Watson/CLEAN_merck2_short/";
		String sourceDirPath = "/home/pilatus/WORK/DATA/Merck_IBM-Watson/merck2_short/";
		
		File[] files = new File(sourceDirPath).listFiles();
		
		for (int i=0; i < files.length; i++){
			sop("" + i + " of "+ files.length);
			File htmlInputFile = new File(files[i].getAbsolutePath());
			Document dirtyDoc = Jsoup.parse(htmlInputFile, "utf-8");
			StringBuffer cleaned = extractCleanHtml(dirtyDoc);
			Files.write(Paths.get(targetDirPath + files[i].getName()), cleaned.toString().getBytes());	
		}

	}

	private static StringBuffer extractCleanHtml (org.jsoup.nodes.Document dirtyDoc){

		String[] excludeList = new String[]{
				"Resources In This Article", 
				"Was This Page Helpful?",
				"Also of Interest",
				"Test your knowledge",
				"Merck and the Merck Manuals", 
				"Professional Version", 
				"Commonly Searched Drugs",
				"Recent News",
				"PROCEDURES & EXAMS",
				"Quizzes",
				"Cases", 
				"Drugs Mentioned In This Article"
		};

		Set<String> excludeSet = new HashSet<String>(Arrays.asList(excludeList));

		StringBuffer cleanedHtml = new StringBuffer("<!DOCTYPE html> <html lang='en-US'> \n");

		cleanedHtml.append(dirtyDoc.select("title").first().toString() + "\n");

		for (Element el : dirtyDoc.getAllElements()){
			String tempTagName = el.tagName();

			if (tempTagName.equals("h1") || tempTagName.equals("h2") || tempTagName.equals("h3") || tempTagName.equals("h4")) {

				String headlineText = el.text().toString();
				if (! excludeSet.contains(headlineText)){
					cleanedHtml.append("<" + tempTagName + ">" + headlineText + "</" + tempTagName + ">\n");
				}

			}else if (tempTagName.equals("div")   ){
				String attributeClass = el.attr("class");
//				sop("DIV");
				if (attributeClass.equals("para")){


					cleanedHtml.append(el.select("p").first().toString() + "\n");

					//					for (Element paragraph : el.getAllElements()){
					//						sop(paragraph.tagName() + "\t" + paragraph.text());
					//					}
					//					sop("-------------");
				}else if (attributeClass.equals("table")){
					//					sop(el.html().toString());
					//					sop("-------------");
				}else{

				}

			}else{

			}
		}



		cleanedHtml.append("</html>");

		return cleanedHtml;
	}

	private static void sop(String msg){
		System.out.println(msg);
	}
}
