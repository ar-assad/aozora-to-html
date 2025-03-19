import java.util.*;
public class AozoraBunkoRuby {
	
	private String text;
//	private int dbgInt = 0;
	
	private List<Integer> liKanjiStart;
	private List<Integer> liFuriganaOpening;
	private List<Integer> liFuriganaClosing;
	private List<Integer> liBoutenOpening;
	private List<Integer> liBoutenClosing;
	private List<Integer> liKanjiBou;
	
	private HashMap<String, String> tenStyles;
	private HashMap<String, String> senStyles;
	
	private static final String BOUTEN = "\u508d\u70b9";
	private static final String BOUSEN = "\u7dda";
	private static final String SPACING = "\u5B57\u4E0B\u3052";
	
	private static final String FW_INTS = "\uFF10\uFF11\uFF12\uFF13\uFF14\uFF15\uFF16\uFF17\uFF18\uFF19";
	
	public AozoraBunkoRuby(String text) {
		this.text = text;
		
		this.liKanjiStart = new ArrayList<>();
		this.liFuriganaOpening = new ArrayList<>();
		this.liFuriganaClosing = new ArrayList<>();
		this.liBoutenOpening = new ArrayList<>();
		this.liBoutenClosing = new ArrayList<>();
		this.liKanjiBou = new ArrayList<>();

		this.tenStyles = new HashMap<>();
		this.tenStyles.put("\u306b\u4E38\u508D\u70B9", "&#x25CF");
		this.tenStyles.put("\u306b\u767D\u4E38\u508D\u70B9", "&#x25CB");
		this.tenStyles.put("\u306b\u9ED2\u4E09\u89D2\u508D\u70B9", "&#x25B2");
		this.tenStyles.put("\u306b\u767D\u4E09\u89D2\u508D\u70B9", "&#x25B3");
		this.tenStyles.put("\u306b\u4E8C\u91CD\u4E38\u508D\u70B9", "&#x25CE");
		this.tenStyles.put("\u306b\u3070\u3064\u508D\u70B9", "&#x00D7");
		
		this.senStyles = new HashMap<>();
		this.senStyles.put("\u306b\u4e8c\u91cd\u508d\u7dda", "text-decoration-style: double;");
		this.senStyles.put("\u306b\u9396\u7dda", "text-decoration-style: dotted;");
		this.senStyles.put("\u306b\u7834\u7dda", "text-decoration-style: dashed;");
		this.senStyles.put("\u306b\u6ce2\u7dda", "text-decoration-style: wavy;");
	}
	
	private void replacements() {

	// Handle Gothic font markers like 第一章［＃「第一章」はゴシック体］by adding bold MS Gothic font
	this.text = this.text.replaceAll("(?m)^([^［]+)［＃「([^」]+)」はゴシック体］$", "<div style=\"font-family: 'MS Gothic'; font-weight: bold;\">$1</div>");

	// Center-align the text for example ［＃地付き］スニーカー文庫編集部 should center the text スニーカー文庫編集部
	this.text = this.text.replaceAll("(?m)^［＃地付き］([^］]+)$", "<div style=\"text-align: center;\">$1</div>");

	// Replace ［＃ここから地付き］ to center-align the text
    this.text = this.text.replaceAll("［＃ここから地付き］", "<div style=\"text-align: center;\">");

    // Close the div tag for ［＃ここで地付き終わり］
    this.text = this.text.replaceAll("［＃ここで地付き終わり］", "</div>");
	
	// Replace ［＃改ページ］ with a line break <br>
    this.text = this.text.replaceAll("［＃改ページ］", "<br>");
 
	// Match and replace image markers like ［＃表紙（img/01_0001a.jpg）］ and similar ones
    this.text = this.text.replaceAll("［＃[^（]*（(img/[^）]+)）］", "<img src=\"$1\">");

    // Match simpler image markers like ［＃（img/01_289.jpg）］
    this.text = this.text.replaceAll("［＃（(img/[^）]+)）］", "<img src=\"$1\">");
	}
	
	public String parse() {
		this.replacements();
		this.getMarkerIdxs();
		
/*		
		System.out.printf("%d %d %d %d %d %d\n",
			liKanjiStart.size(),
			liFuriganaOpening.size(),
			liFuriganaClosing.size(),
			liBoutenOpening.size(),
			liBoutenClosing.size(),
			liKanjiBou.size());
		
		int count1 = 0, count2 = 0;
		for (int i = 0; i < this.text.length(); i++) {
			if (this.text.charAt(i) == '\u300a') {
				count1++;
			}
			else if (this.text.charAt(i) == '\u300b') {
				count2++;
			}
		}
		System.out.printf("%d %d", count1, count2);
		
		System.out.println(this.text.substring(133130, 133150));
		System.out.println("---------------------------------------------------");
		System.out.println(this.text.substring(133733, 133999));
		*/
		
		StringBuilder sb = new StringBuilder();
		
		int i = 0, j = 0, curr = 0;
		
		int kssize = this.liKanjiStart.size(), kbsize = this.liKanjiBou.size();
		while (i < kssize && j < kbsize) {
			// System.out.printf("%d, %d: %d, [%d, %d], [%d, %d]\n", i, j, curr, liKanjiStart.get(i), liFuriganaClosing.get(i), liKanjiBou.get(j), liBoutenClosing.get(j));
			if (liKanjiStart.get(i) < liKanjiBou.get(j)) {
				sb.append(this.text.substring(curr, liKanjiStart.get(i)));
				sb.append(furiganaToRubyTag(liKanjiStart.get(i), liFuriganaOpening.get(i), liFuriganaClosing.get(i)));
				curr = liFuriganaClosing.get(i) + 1;
				i++;
			}
			else {
				sb.append(this.text.substring(curr, liKanjiBou.get(j)));
				sb.append(boutenToRubyTag(liKanjiBou.get(j), liBoutenOpening.get(j), liBoutenClosing.get(j)));
				curr = liBoutenClosing.get(j) + 1;
				j++;
			}
		}
		
		while (i < this.liKanjiStart.size()) {
			sb.append(this.text.substring(curr, liKanjiStart.get(i)));
			sb.append(furiganaToRubyTag(liKanjiStart.get(i), liFuriganaOpening.get(i), liFuriganaClosing.get(i)));
			curr = liFuriganaClosing.get(i) + 1;
			i++;
		}
		
		
		while (j < this.liKanjiBou.size()) {
			// System.out.printf("%d: %d, %d\n", j, curr, liKanjiBou.get(j));
			if (curr >= liKanjiBou.get(j))
				break;
			sb.append(this.text.substring(curr, liKanjiBou.get(j)));
			sb.append(boutenToRubyTag(liKanjiBou.get(j), liBoutenOpening.get(j), liBoutenClosing.get(j)));
			curr = liBoutenClosing.get(j) + 1;
			j++;
		}
		
		sb.append(this.text.substring(curr));
		
		return sb.toString().replaceAll("\uff5c", "");
	}
	
	public String bookmark(String text) {
		StringBuilder sb = new StringBuilder();
		int curr = 0, count = 1, idx;
		while ((idx = text.indexOf('\u3002', curr)) != -1) {
			// System.out.printf("%d %d\n", curr, idx);
			sb.append(text.substring(curr, idx));
			sb.append("<a name=\"save_" + count + "\" href=\"#save_" + count + "\">&#x3002</a>");
			curr = idx + 1;
			count++;
		}
		return sb.toString();
	}
	
	private void getMarkerIdxs() {
		for (int i = 0; i < text.length(); i++) {
			// <<
			if (this.text.charAt(i) == '\u300a') {
				this.liFuriganaOpening.add(i);
				
				int idx;
				for (idx = i - 1; isCJKIdeograph(this.text.charAt(idx)) && this.text.charAt(idx) != '\uff5c'; idx--);
				if (idx == i - 1) {
					for (idx = i - 1; this.text.charAt(idx) != '\uff5c'; idx--);
				}
				
				this.liKanjiStart.add(idx + 1);

				for (idx = i + 1; this.text.charAt(idx) != '\u300b'; idx++);
				this.liFuriganaClosing.add(idx);
				i = idx + 1;
			}
//			// >>
//			else if (this.text.charAt(i) == '\u300b') {
//				this.liFuriganaClosing.add(i);
//			}
			// [
			else if (this.text.charAt(i) == '\uff3b') {
				this.liBoutenOpening.add(i);
				
				int eidx, ws = -1, we = -1;
				for (eidx = i + 1; this.text.charAt(eidx) != '\uff3d'; eidx++) {
					if (this.text.charAt(eidx) == '\u300c')
						ws = eidx;
					else if (this.text.charAt(eidx) == '\u300d')
						we = eidx;
				}
				String btext = this.text.substring(i + 1, eidx);
				if (btext.endsWith(BOUTEN) || btext.endsWith(BOUSEN))
					this.liKanjiBou.add(i - (we-ws-1));
				else
					this.liKanjiBou.add(i);
				this.liBoutenClosing.add(eidx);
				i = eidx + 1;
			}
		}
	}
	
	private String furiganaToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
		StringBuilder sbFurigana = new StringBuilder();
		int i;
			
		sbFurigana.append("<rp>").append(this.text.charAt(startIndex)).append("</rp><rt>");
		// System.out.printf("%d %d %d %d\n", dbgInt++, kanjiIndex, startIndex, endIndex);
		sbFurigana.append(this.text.substring(startIndex + 1, endIndex));
		sbFurigana.append("</rt><rp>").append(this.text.charAt(endIndex)).append("</rp></ruby>");
		
		sbFurigana.insert(0, "</rb>");
		sbFurigana.insert(0, this.text.substring(kanjiIndex, startIndex));
		sbFurigana.insert(0, "<ruby><rb>");
		
		return sbFurigana.toString();
	}
	
	private String boutenToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
		String btext = this.text.substring(startIndex+1, endIndex);
		int wordStart = btext.indexOf("\u300c");
		int wordEnd = btext.indexOf("\u300d");
		int wordLength = wordEnd - wordStart - 1;
		
		if (kanjiIndex != startIndex) {
			StringBuilder output = new StringBuilder();
			if (btext.endsWith(BOUTEN)) {
				String stylename = btext.substring(wordEnd + 1);
				String style = (this.tenStyles.containsKey(stylename)) ? this.tenStyles.get(stylename) : "&#xFE45;";
				output.append("<ruby><rb>").append(this.text.substring(kanjiIndex, startIndex));
				output.append("<rp>\u300a</rp><rt>");
				for (int i = 0; i < wordLength; i++)
					output.append(style);
				output.append("</rt><rp>\u300b</rp></ruby>");
				return output.toString();
			}
			
			else if (btext.endsWith(BOUSEN)) {
				String stylename = btext.substring(wordEnd + 1);
				String style = (this.senStyles.containsKey(stylename)) ? this.senStyles.get(stylename) : "";
				output.append("<u style=\"").append(style).append("\">").append(this.text.substring(kanjiIndex, startIndex)).append("</u>");
				return output.toString();
			}
		}
		else {
			StringBuilder output = new StringBuilder();
			if (btext.endsWith(SPACING)) {
				int hash = btext.indexOf("\uFF03");
				int ji = btext.indexOf("\u5B57");
				String space = btext.substring(hash+1, ji);
				int value = 0;
				for (int i = 0; i < space.length(); i++) {
					value = value * 10 + FW_INTS.indexOf(space.charAt(i));
				}
				for (int i = 0; i < value; i++)
					output.append(" ");
				
				return output.toString();
			}
		}
		
		return this.text.substring(kanjiIndex, startIndex);
	}
	
	private boolean isCJKIdeograph(char c) {
		return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || c == '\u3005';
	}
	/*
	public static void main(String[] args) {
		String encoding = args[0];
		
		String fileName = args[1];
		System.out.println(fileName);
		Path input = Paths.get(fileName);

        try {
			List<String> lines = Files.readAllLines(input, Charset.forName(encoding));
			StringBuilder textsb = new StringBuilder();
			boolean skip = false;
			for (String line : lines) {
				if (!skip) {
					if (line.startsWith("-")) 
						skip = true;
					else
						textsb.append(line).append("\n");
				} else {
					if (line.startsWith("-")) 
						skip = false;
				}
			}
			String text = textsb.toString();
			AozoraBunkoRuby abp = new AozoraBunkoRuby(text);
			
			StringBuilder sb = new StringBuilder();
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			sb.append("<head>");
			sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
			sb.append("<link rel='stylesheet' type='text/css' href='jnf_style.css' />");
			sb.append("</head>");
			sb.append("<body>");
			
			sb.append(abp.bookmark(abp.parse()));
			
			sb.append("</body>");
			sb.append("</html>");
			
			Path output = Paths.get(args[2]);

			try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
				writer.append(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	*/
}
