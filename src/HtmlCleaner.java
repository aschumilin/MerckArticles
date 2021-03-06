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
		/*
		 * first, remove \r\n tokens from original documents: 
		 	/bin/sed -i 's/\\r\\n//g' ~/WORK/DATA/Merck_IBM-Watson/merck2_short/*
		 * after that, extract these elements only:
		 * - title
		 * - h1, h2, h3
		 * - div class="para" (and all nested p elements)
		 * enclose in <!DOCTYPE html> <html lang="en-US">
		 */

	public static void main(String[] args) throws IOException {
		
//		String testPath = "~/WORK/DATA/Merck_IBM-Watson/Cardiovascular_Examination-Cardiovascular-Disorders_original.html";

		
		String targetDirPath = "~/WORK/DATA/Merck_IBM-Watson/CLEAN_merck2_short/";
		String sourceDirPath = "~/WORK/DATA/Merck_IBM-Watson/merck2_short/";
		
		File[] files = new File(sourceDirPath).listFiles();
		
		// iterate over individual files
		for (int i=0; i < files.length; i++){
			sop("cleaning file " + i + " of "+ files.length);
			
			// parse original html with jsoup library
			File htmlInputFile = new File(files[i].getAbsolutePath());
			Document dirtyDoc = Jsoup.parse(htmlInputFile, "utf-8");
			
			// do extraction
			StringBuffer cleaned = extractCleanHtml(dirtyDoc);
			
			// write the result file
			Files.write(Paths.get(targetDirPath + files[i].getName()), cleaned.toString().getBytes());	
		}

	}

	private static StringBuffer extractCleanHtml (org.jsoup.nodes.Document dirtyDoc){

		// exclude headlines with the following text:
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

				if (attributeClass.equals("para")){

					cleanedHtml.append(el.select("p").first().toString() + "\n");

				}else if (attributeClass.equals("table")){
					// dont do tables
				}else{
					// skip
				}
			}else{
				// skip
			}
		}

		cleanedHtml.append("</html>");

		return cleanedHtml;
	}

	private static void sop(String msg){
		System.out.println(msg);
	}
}
