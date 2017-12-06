package com.ruixus.smarty4j.util;

import java.util.HashMap;
import java.util.Map;

public class HTMLEscape {

	private static final Map<String, Character> chars = new HashMap<String, Character>();

	static {
		chars.put("&quot;", '"');
//		chars.put("&#39;", '\'');
		chars.put("&lt;", '<');
		chars.put("&gt;", '>');
		chars.put("&amp;", '&');

		chars.put("&nbsp;", ' ');
		chars.put("&iexcl;", (char) 161);
		chars.put("&cent;", (char) 162);
		chars.put("&pound;", (char) 163);
		chars.put("&curren;", (char) 164);
		chars.put("&yen;", (char) 165);
		chars.put("&brvbar;", (char) 166);
		chars.put("&sect;", (char) 167);
		chars.put("&uml;", (char) 168);
		chars.put("&copy;", (char) 169);
		chars.put("&ordf;", (char) 170);
		chars.put("&laquo;", (char) 171);
		chars.put("&not;", (char) 172);
		chars.put("&shy;", (char) 173);
		chars.put("&reg;", (char) 174);
		chars.put("&macr;", (char) 175);
		chars.put("&deg;", (char) 176);
		chars.put("&plusmn;", (char) 177);
		chars.put("&sup2;", (char) 178);
		chars.put("&sup3;", (char) 179);
		chars.put("&acute;", (char) 180);
		chars.put("&micro;", (char) 181);
		chars.put("&para;", (char) 182);
		chars.put("&middot;", (char) 183);
		chars.put("&cedil;", (char) 184);
		chars.put("&sup1;", (char) 185);
		chars.put("&ordm;", (char) 186);
		chars.put("&raquo;", (char) 187);
		chars.put("&frac14;", (char) 188);
		chars.put("&frac12;", (char) 189);
		chars.put("&frac34;", (char) 190);
		chars.put("&iquest;", (char) 191);
		chars.put("&Agrave;", (char) 192);
		chars.put("&Aacute;", (char) 193);
		chars.put("&Acirc;", (char) 194);
		chars.put("&Atilde;", (char) 195);
		chars.put("&Auml;", (char) 196);
		chars.put("&Aring;", (char) 197);
		chars.put("&AElig;", (char) 198);
		chars.put("&Ccedil;", (char) 199);
		chars.put("&Egrave;", (char) 200);
		chars.put("&Eacute;", (char) 201);
		chars.put("&Ecirc;", (char) 202);
		chars.put("&Euml;", (char) 203);
		chars.put("&Igrave;", (char) 204);
		chars.put("&Iacute;", (char) 205);
		chars.put("&Icirc;", (char) 206);
		chars.put("&Iuml;", (char) 207);
		chars.put("&ETH;", (char) 208);
		chars.put("&Ntilde;", (char) 209);
		chars.put("&Ograve;", (char) 210);
		chars.put("&Oacute;", (char) 211);
		chars.put("&Ocirc;", (char) 212);
		chars.put("&Otilde;", (char) 213);
		chars.put("&Ouml;", (char) 214);
		chars.put("&times;", (char) 215);
		chars.put("&Oslash;", (char) 216);
		chars.put("&Ugrave;", (char) 217);
		chars.put("&Uacute;", (char) 218);
		chars.put("&Ucirc;", (char) 219);
		chars.put("&Uuml;", (char) 220);
		chars.put("&Yacute;", (char) 221);
		chars.put("&THORN;", (char) 222);
		chars.put("&szlig;", (char) 223);
		chars.put("&agrave;", (char) 224);
		chars.put("&aacute;", (char) 225);
		chars.put("&acirc;", (char) 226);
		chars.put("&atilde;", (char) 227);
		chars.put("&auml;", (char) 228);
		chars.put("&aring;", (char) 229);
		chars.put("&aelig;", (char) 230);
		chars.put("&ccedil;", (char) 231);
		chars.put("&egrave;", (char) 232);
		chars.put("&eacute;", (char) 233);
		chars.put("&ecirc;", (char) 234);
		chars.put("&euml;", (char) 235);
		chars.put("&igrave;", (char) 236);
		chars.put("&iacute;", (char) 237);
		chars.put("&icirc;", (char) 238);
		chars.put("&iuml;", (char) 239);
		chars.put("&eth;", (char) 240);
		chars.put("&ntilde;", (char) 241);
		chars.put("&ograve;", (char) 242);
		chars.put("&oacute;", (char) 243);
		chars.put("&ocirc;", (char) 244);
		chars.put("&otilde;", (char) 245);
		chars.put("&ouml;", (char) 246);
		chars.put("&divide;", (char) 247);
		chars.put("&oslash;", (char) 248);
		chars.put("&ugrave;", (char) 249);
		chars.put("&uacute;", (char) 250);
		chars.put("&ucirc;", (char) 251);
		chars.put("&uuml;", (char) 252);
		chars.put("&yacute;", (char) 253);
		chars.put("&thorn;", (char) 254);
		chars.put("&yuml;", (char) 255);
		chars.put("&OElig;", (char) 338);
		chars.put("&oelig;", (char) 339);
		chars.put("&Scaron;", (char) 352);
		chars.put("&scaron;", (char) 353);
		chars.put("&Yuml;", (char) 376);
		chars.put("&fnof;", (char) 402);
		chars.put("&circ;", (char) 710);
		chars.put("&tilde;", (char) 732);
		chars.put("&Alpha;", (char) 913);
		chars.put("&Beta;", (char) 914);
		chars.put("&Gamma;", (char) 915);
		chars.put("&Delta;", (char) 916);
		chars.put("&Epsilon;", (char) 917);
		chars.put("&Zeta;", (char) 918);
		chars.put("&Eta;", (char) 919);
		chars.put("&Theta;", (char) 920);
		chars.put("&Iota;", (char) 921);
		chars.put("&Kappa;", (char) 922);
		chars.put("&Lambda;", (char) 923);
		chars.put("&Mu;", (char) 924);
		chars.put("&Nu;", (char) 925);
		chars.put("&Xi;", (char) 926);
		chars.put("&Omicron;", (char) 927);
		chars.put("&Pi;", (char) 928);
		chars.put("&Rho;", (char) 929);
		chars.put("&Sigma;", (char) 931);
		chars.put("&Tau;", (char) 932);
		chars.put("&Upsilon;", (char) 933);
		chars.put("&Phi;", (char) 934);
		chars.put("&Chi;", (char) 935);
		chars.put("&Psi;", (char) 936);
		chars.put("&Omega;", (char) 937);
		chars.put("&alpha;", (char) 945);
		chars.put("&beta;", (char) 946);
		chars.put("&gamma;", (char) 947);
		chars.put("&delta;", (char) 948);
		chars.put("&epsilon;", (char) 949);
		chars.put("&zeta;", (char) 950);
		chars.put("&eta;", (char) 951);
		chars.put("&theta;", (char) 952);
		chars.put("&iota;", (char) 953);
		chars.put("&kappa;", (char) 954);
		chars.put("&lambda;", (char) 955);
		chars.put("&mu;", (char) 956);
		chars.put("&nu;", (char) 957);
		chars.put("&xi;", (char) 958);
		chars.put("&omicron;", (char) 959);
		chars.put("&pi;", (char) 960);
		chars.put("&rho;", (char) 961);
		chars.put("&sigmaf;", (char) 962);
		chars.put("&sigma;", (char) 963);
		chars.put("&tau;", (char) 964);
		chars.put("&upsilon;", (char) 965);
		chars.put("&phi;", (char) 966);
		chars.put("&chi;", (char) 967);
		chars.put("&psi;", (char) 968);
		chars.put("&omega;", (char) 969);
		chars.put("&thetasym;", (char) 977);
		chars.put("&upsih;", (char) 978);
		chars.put("&piv;", (char) 982);
		chars.put("&ensp;", (char) 8194);
		chars.put("&emsp;", (char) 8195);
		chars.put("&thinsp;", (char) 8201);
		chars.put("&zwnj;", (char) 8204);
		chars.put("&zwj;", (char) 8205);
		chars.put("&lrm;", (char) 8206);
		chars.put("&rlm;", (char) 8207);
		chars.put("&ndash;", (char) 8211);
		chars.put("&mdash;", (char) 8212);
		chars.put("&lsquo;", (char) 8216);
		chars.put("&rsquo;", (char) 8217);
		chars.put("&sbquo;", (char) 8218);
		chars.put("&ldquo;", (char) 8220);
		chars.put("&rdquo;", (char) 8221);
		chars.put("&bdquo;", (char) 8222);
		chars.put("&dagger;", (char) 8224);
		chars.put("&Dagger;", (char) 8225);
		chars.put("&bull;", (char) 8226);
		chars.put("&hellip;", (char) 8230);
		chars.put("&permil;", (char) 8240);
		chars.put("&prime;", (char) 8242);
		chars.put("&Prime;", (char) 8243);
		chars.put("&lsaquo;", (char) 8249);
		chars.put("&rsaquo;", (char) 8250);
		chars.put("&oline;", (char) 8254);
		chars.put("&frasl;", (char) 8260);
		chars.put("&euro;", (char) 8364);
		chars.put("&image;", (char) 8465);
		chars.put("&weierp;", (char) 8472);
		chars.put("&real;", (char) 8476);
		chars.put("&trade;", (char) 8482);
		chars.put("&alefsym;", (char) 8501);
		chars.put("&larr;", (char) 8592);
		chars.put("&uarr;", (char) 8593);
		chars.put("&rarr;", (char) 8594);
		chars.put("&darr;", (char) 8595);
		chars.put("&harr;", (char) 8596);
		chars.put("&crarr;", (char) 8629);
		chars.put("&lArr;", (char) 8656);
		chars.put("&uArr;", (char) 8657);
		chars.put("&rArr;", (char) 8658);
		chars.put("&dArr;", (char) 8659);
		chars.put("&hArr;", (char) 8660);
		chars.put("&forall;", (char) 8704);
		chars.put("&part;", (char) 8706);
		chars.put("&exist;", (char) 8707);
		chars.put("&empty;", (char) 8709);
		chars.put("&nabla;", (char) 8711);
		chars.put("&isin;", (char) 8712);
		chars.put("&notin;", (char) 8713);
		chars.put("&ni;", (char) 8715);
		chars.put("&prod;", (char) 8719);
		chars.put("&sum;", (char) 8721);
		chars.put("&minus;", (char) 8722);
		chars.put("&lowast;", (char) 8727);
		chars.put("&radic;", (char) 8730);
		chars.put("&prop;", (char) 8733);
		chars.put("&infin;", (char) 8734);
		chars.put("&ang;", (char) 8736);
		chars.put("&and;", (char) 8743);
		chars.put("&or;", (char) 8744);
		chars.put("&cap;", (char) 8745);
		chars.put("&cup;", (char) 8746);
		chars.put("&int;", (char) 8747);
		chars.put("&there4;", (char) 8756);
		chars.put("&sim;", (char) 8764);
		chars.put("&cong;", (char) 8773);
		chars.put("&asymp;", (char) 8776);
		chars.put("&ne;", (char) 8800);
		chars.put("&equiv;", (char) 8801);
		chars.put("&le;", (char) 8804);
		chars.put("&ge;", (char) 8805);
		chars.put("&sub;", (char) 8834);
		chars.put("&sup;", (char) 8835);
		chars.put("&nsub;", (char) 8836);
		chars.put("&sube;", (char) 8838);
		chars.put("&supe;", (char) 8839);
		chars.put("&oplus;", (char) 8853);
		chars.put("&otimes;", (char) 8855);
		chars.put("&perp;", (char) 8869);
		chars.put("&sdot;", (char) 8901);
		chars.put("&lceil;", (char) 8968);
		chars.put("&rceil;", (char) 8969);
		chars.put("&lfloor;", (char) 8970);
		chars.put("&rfloor;", (char) 8971);
		chars.put("&lang;", (char) 9001);
		chars.put("&rang;", (char) 9002);
		chars.put("&loz;", (char) 9674);
		chars.put("&spades;", (char) 9824);
		chars.put("&clubs;", (char) 9827);
		chars.put("&hearts;", (char) 9829);
		chars.put("&diams;", (char) 9830);
	}
	
	public static char toChar(String name) {
		Character c = chars.get(name);
		return c != null ? c : 0;
	}
}
