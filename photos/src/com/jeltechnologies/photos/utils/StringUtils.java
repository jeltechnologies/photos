package com.jeltechnologies.photos.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    private static final int ENDLESSLOOP_THRESHOLD = 100000;

    public static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzÃ¥Ã¤Ã¶ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789Ã…Ã–Ã„<>/.,-+=()@!:;&'\"";

    public static final String NUMBERS = "0123456789";

    public static final String PUNCTUATION = ",.:;!?";

    public static final String SENTENCE_SPLITTER = ".!?";

    public static final BigDecimal HUNDRED = new BigDecimal(100);

    public static MathContext PERCENTAGE_ROUNDING = new MathContext(3, RoundingMode.HALF_UP);

    public static String formatNumber(Object object) {
	DecimalFormat numberFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
	numberFormatter.setGroupingSize(3);
	return numberFormatter.format(object);
    }

    public static String cropAfter(Object lineObject, int afterPos, boolean addColumnHint) {
	String result;
	if (lineObject == null) {
	    result = "null";
	} else {
	    String line = lineObject.toString();
	    if (line.length() > afterPos) {
		result = line.substring(0, afterPos);
		if (addColumnHint) {
		    result = result + "...";
		}
	    } else {
		result = line;
	    }
	}
	return result;
    }

    public static String replaceEach(String value, char[] specialChars, String[] replacements) {
	StringBuilder resultBuilder = new StringBuilder();
	for (int charIndex = 0; charIndex < value.length(); charIndex++) {
	    char c = value.charAt(charIndex);
	    int replacementIndex = -1;

	    for (int specialCharIndex = 0; specialCharIndex < specialChars.length && replacementIndex == -1; specialCharIndex++) {
		if (specialChars[specialCharIndex] == c) {
		    replacementIndex = specialCharIndex;
		}
	    }

	    if (replacementIndex == -1) {
		resultBuilder.append(c);
	    } else {
		resultBuilder.append(replacements[replacementIndex]);
	    }
	}
	return resultBuilder.toString();
    }

    public static String stripControlChars(String html) {
	if (html == null) {
	    return null;
	} else {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < html.length(); i++) {
		char c = html.charAt(i);
		if (c >= ' ') {
		    builder.append(c);
		}
	    }
	    String strippedHtml = builder.toString();
	    return strippedHtml;
	}
    }

    public static String stripNumbers(String html) {
	StringBuilder builder = new StringBuilder();
	for (int i = 0; i < html.length(); i++) {
	    char c = html.charAt(i);
	    if (c < '0' || c > '9') {
		builder.append(c);
	    }
	}
	String strippedHtml = builder.toString();
	return strippedHtml;
    }

    public static String stripNonAscii(String html) {
	StringBuilder builder = new StringBuilder();
	boolean lastTokenWasSpace = false;
	for (int i = 0; i < html.length(); i++) {
	    char c = html.charAt(i);
	    if (c > ' ' && c < 255) {
		builder.append(c);
		lastTokenWasSpace = false;
	    } else {
		if ((c == '\r') || (c == '\n') || (c == ' ') || (c == '\t')) {
		    if (!lastTokenWasSpace) {
			builder.append(' ');
		    }
		    lastTokenWasSpace = true;
		}
	    }
	}
	String strippedHtml = builder.toString();
	return strippedHtml;
    }

    public static String stripDoubleSpaces(String html) {
	StringBuilder builder = new StringBuilder();
	boolean lastCharWasSpace = false;
	for (int i = 0; i < html.length(); i++) {
	    char c = html.charAt(i);
	    if (c >= ' ') {
		boolean skip = (c == ' ' && lastCharWasSpace);
		if (!skip) {
		    builder.append(c);
		}
		lastCharWasSpace = c == ' ';
	    }
	}
	String strippedHtml = builder.toString();
	return strippedHtml;
    }

    public static String removeSHOUTING(String original) {
	// Make sure to remove all none ascii things
	StringBuilder allowedCharBuilder = new StringBuilder();
	for (int i = 0; i < original.length(); i++) {
	    char currentToken = original.charAt(i);
	    boolean allowed = ALLOWED_CHARS.indexOf(currentToken) > -1;
	    if (allowed) {
		allowedCharBuilder.append(currentToken);
	    } else {
		allowedCharBuilder.append(' ');
	    }
	}
	String allowedString = allowedCharBuilder.toString();

	// remove double spaces, !! and replace ! with .
	StringBuilder resultBuilder = new StringBuilder();
	char lastToken = ' ';
	for (int i = 0; i < allowedString.length(); i++) {
	    char currentToken = allowedString.charAt(i);
	    boolean skip = (lastToken == '!' && currentToken == '!') || (lastToken == ' ' && currentToken == ' ');
	    if (!skip) {
		if (currentToken == '!') {
		    resultBuilder.append(".");
		} else {
		    resultBuilder.append(currentToken);
		}
	    }
	    lastToken = currentToken;
	}
	String removedExcl = resultBuilder.toString();

	// remove CAPITALS
	String spaceTaggedOriginal = removedExcl.replaceAll(">", "> ");
	StringBuilder normalCaseBuilder = new StringBuilder();
	StringTokenizer tokenizer = new StringTokenizer(spaceTaggedOriginal);
	boolean first = true;
	while (tokenizer.hasMoreElements()) {
	    String currentWord = tokenizer.nextToken();
	    if (!currentWord.equals("")) {
		if (currentWord.length() > 1) {
		    char firstChar = currentWord.charAt(0);
		    char secondChar = currentWord.charAt(1);
		    boolean firstUpperCase = firstChar >= 'A' && firstChar <= 'Z';
		    boolean secondUpperCase = secondChar >= 'A' && secondChar <= 'Z';
		    if (firstUpperCase && secondUpperCase) {
			String sentence = normalCaseBuilder.toString();
			boolean newSentence = first || sentence.endsWith(".") || sentence.endsWith("!") || sentence.endsWith("<br/>");
			if (newSentence) {
			    currentWord = firstChar + currentWord.substring(1).toLowerCase();
			} else {
			    currentWord = currentWord.toLowerCase();
			}
		    }
		}
		if (first) {
		    first = false;
		} else {
		    normalCaseBuilder.append(" ");
		}
		normalCaseBuilder.append(currentWord);
	    }
	}
	// remove the spaces after tags again
	String result = normalCaseBuilder.toString().replaceAll("> ", ">");

	return result.toString();
    }

    public static String stripHtmlAndSpaces(String html) {
	String strippedHtml = stripNonAscii(html);
	StringBuilder builder = new StringBuilder();
	for (int i = 0; i < strippedHtml.length(); i++) {
	    char c = strippedHtml.charAt(i);
	    if (c > ' ') {
		builder.append(c);
	    }
	}
	return builder.toString();
    }

    /**
     * In a string find text between two strings, the start and end string.
     * <p>
     * This method starts searching at start and then finds the end.
     * 
     * @param searchIn the text to search in
     * @param start    the start of the text to search between
     * @param end      the end of the text to search between
     * @return the text between start and end, without start and end itself
     */
    public static String findBetween(String searchIn, String start, String end) {
	String result = "";
	int startPos = searchIn.indexOf(start);
	if (startPos > -1) {
	    int endPos = searchIn.indexOf(end, startPos + start.length());
	    if (endPos > -1) {
		result = searchIn.substring(startPos + start.length(), endPos);
	    }
	}
	return result;
    }

    /**
     * In a string find text between two strings, the start and end string.
     * 
     * @param searchIn      the text to search in
     * @param start         the start of the text to search between
     * @param end           the end of the text to search between
     * @param searchAtStart start search at start (true) or at end (false)
     * @return the text between start and end, without start and end itself
     */
    public static String findBetween(String searchIn, String start, String end, boolean searchAtStart) {
	if (searchAtStart) {
	    return findBetween(searchIn, start, end);
	} else {
	    String result = "";
	    // search from end
	    int endPos = searchIn.indexOf(end);
	    if (endPos > -1) {
		int currentPos = endPos;
		boolean found = false;
		while (currentPos >= 0 && !found) {
		    String currentString = searchIn.substring(currentPos, endPos);
		    if (currentString.startsWith(start)) {
			found = true;
		    } else {
			currentPos--;
		    }
		}
		if (found) {
		    result = searchIn.substring(currentPos + start.length(), endPos);
		}
	    }
	    return result;
	}
    }

    /**
     * Find all texts between two strings, the start and end string.
     * 
     * @param searchIn the text to search in
     * @param start    the start of the text to search between
     * @param end      the end of the text to search between
     * @return a List of Strings containing all occurances between start and end, but without start and end itself.
     */
    public static List<String> findAllBetween(String searchIn, String start, String end, boolean includeStartAndEnd) {
	List<String> result = new ArrayList<String>();
	if (searchIn != null) {
	    int startPos = 0;
	    int counter = 0;
	    while (startPos > -1 && startPos < searchIn.length()) {
		startPos = searchIn.indexOf(start, startPos);
		if (startPos > -1) {
		    int endPos = searchIn.indexOf(end, startPos + start.length());
		    if (endPos > -1) {
			String between = searchIn.substring(startPos + start.length(), endPos);
			if (includeStartAndEnd) {
			    between = start + between + end;
			}
			result.add(between);
			startPos = endPos;
		    } else {
			startPos++; // to prevent endless loop
		    }
		}
		counter++;
		if (counter > ENDLESSLOOP_THRESHOLD) {
		    String errorMessage = "Endless loop found in findAllBetween counter: " + counter + ", searchIn: [" + searchIn + "], start: [" + start
			    + "], end: [" + end + "]";
		    LOGGER.error(errorMessage);
		    throw new RuntimeException(errorMessage);
		}
	    }
	}
	return result;
    }

    public static boolean isEmpty(String in) {
	boolean result = false;
	if (in == null) {
	    result = true;
	} else {
	    String trimmed = in.trim();
	    result = trimmed.length() == 0;
	}
	return result;
    }

    public static List<String> findAllBetween(String searchIn, String start, String end) {
	return findAllBetween(searchIn, start, end, false);
    }

    public static String stripAfter(String in, String after) {
	String result;
	int startStrip = in.indexOf(after);
	if (startStrip == 0) {
	    result = "";
	} else {
	    if (startStrip < 0) {
		result = in;
	    } else {
		result = in.substring(0, startStrip);
	    }
	}
	return result;
    }

    public static String stripBefore(String in, String before) {
	String result;
	int startStrip = in.indexOf(before);

	if (startStrip < 0) {
	    result = in;
	} else {
	    result = in.substring(startStrip + before.length());
	}
	return result;
    }

    public static boolean isEmptyTrimmed(String in) {
	boolean result;
	if (in == null) {
	    result = true;
	} else {
	    String trimmed = in.trim();
	    result = trimmed.isEmpty();
	}
	return result;
    }

    public static String stripBeforeLast(String in, String after) {
	String result;
	int lastPos = in.lastIndexOf(after);
	if (lastPos > -1 && lastPos < (in.length() - 1)) {
	    result = in.substring(lastPos + 1);
	} else {
	    result = "";
	}
	return result;
    }

    public static String stripAfterLast(String in, String after) {
	String result;
	int lastPos = in.lastIndexOf(after);
	if (lastPos > -1 && lastPos < (in.length() - 1)) {
	    result = in.substring(0, lastPos);
	} else {
	    result = in;
	}
	return result;
    }

    public static String findAfter(String in, String after) {
	String result;
	if (in == null) {
	    result = "";
	} else {
	    int startAfter = in.indexOf(after);
	    if (startAfter > -1) {
		result = in.substring(startAfter + after.length());
	    } else {
		result = "";
	    }
	}
	return result;
    }

    public static String findAfterLast(String in, String after) {
	String result;
	int startAfter = in.lastIndexOf(after);
	if (startAfter > -1) {
	    result = in.substring(startAfter + after.length());
	} else {
	    result = "";
	}
	return result;
    }

    public static String findAfterIfNotFoundReturnIn(String in, String after) {
	String result;
	int startAfter = in.indexOf(after);
	if (startAfter > -1) {
	    result = in.substring(startAfter + after.length());
	} else {
	    result = in;
	}
	return result;
    }

    public static String findAfterLastIfNotFoundReturnIn(String in, String after) {
	String result;
	int startAfter = in.lastIndexOf(after);
	if (startAfter > -1) {
	    result = in.substring(startAfter + after.length());
	} else {
	    result = in;
	}
	return result;
    }

    public static String stripAllOccurances(String in, String from, String to) {
	String result = in;
	int startPos = 0;
	int endPos = -1;
	while (startPos > -1) {
	    startPos = result.indexOf(from, startPos);
	    if (startPos > -1) {
		endPos = result.indexOf(to, startPos);
		if (endPos > -1) {
		    String newResult = result.substring(0, startPos) + result.substring(endPos + to.length());
		    result = newResult;
		    startPos++;
		} else {
		    startPos++;
		}
	    }
	}
	return result;
    }

    public static String stripAllOccurances(String in, String textToStrip) {
	String result;
	if (in == null) {
	    result = "";
	} else {
	    if (textToStrip == null || textToStrip.isEmpty()) {
		result = in;
	    } else {
		StringBuilder builder = new StringBuilder();
		int pos = 0;
		boolean found = true;
		int loopCounter = 0;
		while (found) {
		    int foundPos = in.indexOf(textToStrip, pos);
		    if (foundPos == -1) {
			found = false;
		    } else {
			builder.append(in.substring(pos, foundPos));
			pos = foundPos + textToStrip.length();
		    }
		    loopCounter++;
		    if (loopCounter > ENDLESSLOOP_THRESHOLD) {
			throw new RuntimeException("Internal error, endless loop detected, crash hard");
		    }
		}
		builder.append(in.substring(pos));
		result = builder.toString();
	    }
	}
	return result;
    }

    public static String stripChar(String in, char tokenToStrip) {
	StringBuilder builder = new StringBuilder();
	for (int pos = 0; pos < in.length(); pos++) {
	    char c = in.charAt(pos);
	    if (c != tokenToStrip) {
		builder.append(c);
	    }
	}
	return builder.toString();
    }

    public static List<String> splitIntoSentences(String in) {
	List<String> sentences = new ArrayList<String>();
	if (in != null) {
	    StringBuilder builder = new StringBuilder();
	    int pos = 0;
	    int length = in.length();
	    int lengthMinOne = length - 1;
	    while (pos < length) {
		char c = in.charAt(pos);
		builder.append(c);
		if (SENTENCE_SPLITTER.indexOf(c) > -1) {
		    if (pos < lengthMinOne) {
			char nextChar = in.charAt(pos + 1);
			if (NUMBERS.indexOf(nextChar) == -1) {
			    String sentence = builder.toString().trim();
			    if (sentence.length() > 1) {
				sentences.add(sentence);
			    }
			    builder = new StringBuilder();
			}
		    }
		}
		pos++;
	    }
	    String sentence = builder.toString().trim();
	    if (sentence.length() > 1) {
		sentences.add(sentence);
	    }
	}
	return sentences;
    }

    public static String fixPunctuation(String in) {
	StringBuilder builder = new StringBuilder();
	int pos = 0;
	int length = in.length();
	int lengthMinOne = length - 1;
	while (pos < length) {
	    char c = in.charAt(pos);
	    builder.append(c);
	    if (PUNCTUATION.indexOf(c) > -1) {
		if (pos < lengthMinOne) {
		    char nextChar = in.charAt(pos + 1);
		    if (NUMBERS.indexOf(nextChar) == -1) {
			builder.append(" ");
		    }
		}
	    }
	    pos++;
	}
	String withoutDoubleSpaces = stripDoubleSpaces(builder.toString());
	return withoutDoubleSpaces;
    }

    public static String fixFirstCharUpperCase(String in) {
	StringBuilder builder = new StringBuilder();
	if (in != null) {
	    boolean nextTokenMustBeUpper = true;
	    for (int pos = 0; pos < in.length(); pos++) {
		char c = in.charAt(pos);
		String s = String.valueOf(c);
		if (nextTokenMustBeUpper) {
		    builder.append(s.toUpperCase());
		} else {
		    builder.append(s.toLowerCase());
		}
		nextTokenMustBeUpper = (c == ' ') || (c == '-');
	    }
	}
	return builder.toString().trim();
    }

    public static String stripTagsFromParagraph(String in) {
	StringBuilder builder = new StringBuilder();
	boolean inTag = false;
	int pos = 0;
	while (pos < in.length()) {
	    char c = in.charAt(pos);
	    boolean mustBeAdded;
	    if (inTag) {
		if (c == '>') {
		    inTag = false;
		}
		mustBeAdded = false;
	    } else {
		if (c == '<') {
		    inTag = true;
		    mustBeAdded = false;
		} else {
		    mustBeAdded = true;
		}
	    }
	    if (mustBeAdded) {
		builder.append(c);
	    }
	    pos++;
	}

	return builder.toString();
    }

    public static String stripSpaces(String in) {
	StringBuilder builder = new StringBuilder();
	int pos = 0;
	while (pos < in.length()) {
	    char c = in.charAt(pos);
	    if (c != ' ') {
		builder.append(c);
	    }
	    pos++;
	}
	return builder.toString();
    }

    public static String decodeURL(String in) {
	String result;
	try {
	    result = URLDecoder.decode(in, "UTF-8");
	} catch (Exception exception) {
	    result = in;
	    LOGGER.debug("Ignoring URL decode for [" + "]", exception);
	}
	return result;
    }

    public static String encodeURL(String in) {
	String result;
	try {
	    result = URLEncoder.encode(in, "UTF-8");
	} catch (Exception exception) {
	    result = in;
	    LOGGER.debug("Ignoring URL encode for [" + "]", exception);
	}
	return result;
    }

    public static String replaceAll(String in, char searchFor, char replaceBy) {
	return replaceAll(in, searchFor, String.valueOf(replaceBy));
    }

    public static String replaceAll(String in, char searchFor, String replaceBy) {
	StringBuilder builder = new StringBuilder();
	if (in != null) {
	    int pos = 0;
	    while (pos < in.length()) {
		char c = in.charAt(pos);
		if (c == searchFor) {
		    builder.append(replaceBy);
		} else {
		    builder.append(c);
		}
		pos++;
	    }
	}
	return builder.toString();
    }

    public static String replaceAll(String in, String searchFor, String replaceBy) {
	String result = in.replaceAll(searchFor, replaceBy);
	return result;
    }

    public static String addEscapes(String value) {
	StringBuilder result = new StringBuilder();
	for (int i = 0; i < value.length(); i++) {
	    char c = value.charAt(i);
	    String r;
	    switch (c) {
		case '\b':
		    r = "\\b";
		    break;
		case '\f':
		    r = "\\f";
		    break;
		case '\n':
		    r = "\\n";
		    break;
		case '\r':
		    r = "\\r";
		    break;
		case '\t':
		    r = "\\t";
		    break;
		case '"':
		    r = "\\\"";
		    break;
		case '\\':
		    r = "\\\\";
		    break;
		default:
		    r = String.valueOf(c);
	    }
	    result.append(r);
	}
	return result.toString();
    }

    /**
     * Take a string and add all numbers. Ignore everything after , or . just like the (int) in Java.
     * 
     * @param in
     * @return
     */
    public static String stripToIntegerString(String in) {
	if (in == null) {
	    return "";
	} else {
	    StringBuilder builder = new StringBuilder();
	    boolean fractionFound = false;
	    for (int pos = 0; !fractionFound && pos < in.length(); pos++) {
		char c = in.charAt(pos);
		if (c == '.' || c == ',') {
		    fractionFound = true;
		} else {
		    if (NUMBERS.indexOf(c) > -1) {
			builder.append(c);
		    }
		}
	    }
	    return builder.toString();
	}
    }

    /**
     * Take a string and add all numbers. Ignore everything after , or . just like the (int) in Java.
     * 
     * @param in
     * @return
     */
    public static int stripToInteger(String in) {
	String integerString = stripToIntegerString(in);
	return Integer.parseInt(integerString);
    }

    public static String toMilliseconds(long startTimeNano, long endTimeNano) {
	long elapsedTime = endTimeNano - startTimeNano;
	long milliseconds = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
	return milliseconds + " ms";
    }

    public static int toInt(String s, int fallback) {
	int i;
	if (s == null) {
	    i = fallback;
	} else {
	    try {
		i = Integer.valueOf(s);
	    } catch (Exception nfe) {
		i = fallback;
	    }
	}
	return i;
    }

    public static List<String> splitToLines(String in) {
	List<String> lines = new ArrayList<String>();
	if (in != null) {
	    StringBuilder builder = new StringBuilder();
	    for (int pos = 0; pos < in.length(); pos++) {
		char c = in.charAt(pos);
		if (c == '\n') {
		    lines.add(builder.toString());
		    builder = new StringBuilder();
		} else {
		    if (c != '\r') {
			builder.append(c);
		    }
		}
	    }
	    String lastLine = builder.toString();
	    if (!lastLine.isEmpty()) {
		lines.add(lastLine);
	    }
	}
	return lines;
    }

    public static String getValueEmptyIfNull(String in) {
	String result;
	if (in == null) {
	    result = "";
	} else {
	    result = in;
	}
	return result;
    }

    public static String dup(int length, char c) {
	StringBuilder b = new StringBuilder();
	for (int i = 0; i < length; i++) {
	    b.append(c);
	}
	return b.toString();
    }

    public static String dup(int length, String s) {
	StringBuilder b = new StringBuilder();
	for (int i = 0; i < length; i++) {
	    b.append(s);
	}
	return b.toString();
    }

    public static List<String> split(String in, char devider) {
	List<String> lines = new ArrayList<String>();
	if (in != null) {
	    StringBuilder builder = new StringBuilder();
	    for (int pos = 0; pos < in.length(); pos++) {
		char c = in.charAt(pos);
		if (c == devider) {
		    if (builder.length() > 0) {
			lines.add(builder.toString());
			builder = new StringBuilder();
		    }
		} else {
		    builder.append(c);
		}
	    }
	    String lastLine = builder.toString();
	    if (!lastLine.isEmpty()) {
		lines.add(lastLine);
	    }
	}
	return lines;
    }

    public static boolean containsValue(String s) {
	return s != null && !s.trim().equals("");
    }

}
