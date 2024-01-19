package com.hti.smpp.common.util;

import java.util.Enumeration;
import java.util.Hashtable;

// Done by Amit_vish on date 13-DEC-11

public class GMTmapping {

	public static String getGMT(String key) {
		String gmtValue = "";
		String key1 = "";
		Hashtable table = new Hashtable();
		table.put("GMT+02:00", "(GMT+02:00)Beirut");
		table.put("GMT-12:00", "(GMT-12:00)InternationalDateLineWest");
		table.put("GMT-11:00", "(GMT-11:00)MidwayIsland,Samoa");
		table.put("GMT-10:00", "(GMT-10:00)Hawaii");
		table.put("GMT-09:00", "(GMT-09:00)Alaska");
		table.put("GMT-08:00", "(GMT-08:00)PacificTime(US&Canada)");
		table.put("GMT-08:00", "(GMT-08:00)TijuanaBajaCalifornia");
		table.put("GMT-07:00", "(GMT-07:00)Arizona");
		table.put("GMT-07:00", "(GMT-07:00)Chihuahua,Lapaz,Mazatlan");
		table.put("GMT-07:00", "(GMT-07:00)MountainTime(US&Canada)");
		table.put("GMT-06:00", "(GMT-06:00)CentralAmerica");
		table.put("GMT-06:00", "(GMT-06:00)CentralTime(US&Canada)");
		table.put("GMT-06:00", "(GMT-06:00)Guadalajara,MexicoCity,Monterrey");
		table.put("GMT-06:00", "(GMT-06:00)Saskatchewan");
		table.put("GMT-05:00", "(GMT-05:00)Bogota,Lima,Quito,RioBranco");
		table.put("GMT-05:00", "(GMT-05:00)EasternTime(US&Canada)");
		table.put("GMT-05:00", "(GMT-05:00)Indiana(East)");
		table.put("GMT-04:30", "(GMT-04:30)Caracas");
		table.put("GMT-04:00", "(GMT-04:00)AtlenticTime(Canada)");
		table.put("GMT-04:00", "(GMT-04:00)LaPaz");
		table.put("GMT-04:00", "(GMT-04:00)Manaus");
		table.put("GMT-04:00", "(GMT-04:00)Santiago");
		table.put("GMT-03:30", "(GMT-03:30)Newfoundland");
		table.put("GMT-03:00", "(GMT-03:00)Brasilia");
		table.put("GMT-03:00", "(GMT-03:00)BuenosAires,Georgetown");
		table.put("GMT-03:00", "(GMT-03:00)Greenland");
		table.put("GMT-03:00", "(GMT-03:00)Montevideo");
		table.put("GMT-02:00", "(GMT-02:00)mid-Atlantic");
		table.put("GMT-01:00", "(GMT-01:00)Azores");
		table.put("GMT-01:00", "(GMT-01:00)CapeVerdeIs");
		table.put("GMT", "(GMT)Casablanca,Monrovia,Reykjavik");
		table.put("GMT", "(GMT)GreenwichMeanTime:Bublin,Edinburgh,Lisban,London");
		table.put("GMT+01:00", "(GMT+01:00)Amsterdam,Berlin,Bern,Rome,Stockholm,Vienna");
		table.put("GMT+01:00", "(GMT+01:00)Belgrade,Bratislava,Budapest,Ljubljana,Prague");
		table.put("GMT+01:00", "(GMT+01:00)Brassels,Copenhegen,Mardrid,Paris");
		table.put("GMT+01:00", "(GMT+01:00)Sarajevo,Skopje,Warsaw,Zagreb");
		table.put("GMT+01:00", "(GMT+01:00)WestCentralAfrica");
		table.put("GMT+02:00", "(GMT+02:00)Amman");
		table.put("GMT+02:00", "(GMT+02:00)Athens,Bucharest,Istanbul");
		table.put("GMT+02:00", "(GMT+02:00)Cairo");
		table.put("GMT+02:00", "(GMT+02:00)Harare,Pretoria");
		table.put("GMT+02:00", "(GMT+02:00)Helsinki,Kyiv,Riga,Sofia,Tallinn,Vilnius");
		table.put("GMT+02:00", "(GMT+02:00)Jerusalem");
		table.put("GMT+02:00", "(GMT+02:00)Minsk");
		table.put("GMT+02:00", "(GMT+02:00)Windhoek");
		table.put("GMT+03:00", "(GMT+03:00)Baghdad");
		table.put("GMT+03:00", "(GMT+03:00)Kuwait,Riyadh");
		table.put("GMT+03:00", "(GMT+03:00)Moscow,St.Petersburg,Volgograd");
		table.put("GMT+03:00", "(GMT+03:00)Nairobi");
		table.put("GMT+03:00", "(GMT+03:00)Tbilisi");
		table.put("GMT+03:30", "(GMT+03:30)Tehran");
		table.put("GMT+04:00", "(GMT+04:00)AbuDhabi,Muscat");
		table.put("GMT+04:00", "(GMT+04:00)Baku");
		table.put("GMT+04:00", "(GMT+04:00)Yerevan");
		table.put("GMT+04:30", "(GMT+04:30)Kabul");
		table.put("GMT+05:00", "(GMT+05:00)Ekaterinburg");
		table.put("GMT+05:00", "(GMT+05:00)Islamabad,Karachi,Tashkent");
		table.put("GMT+05:30", "(GMT+05:30)Chennai,Kolkata,Mumbai,NewDelhi");
		table.put("GMT+05:30", "(GMT+05:30)SriJayawardenepura");
		table.put("GMT+05:45", "(GMT+05:45)Kathmandu");
		table.put("GMT+06:00", "(GMT+06:00)Almaty,Novosibirsk");
		table.put("GMT+06:00", "(GMT+06:00)Astana,Dhaka");
		table.put("GMT+06:30", "(GMT+06:30)Yangon(Ranfoon)");
		table.put("GMT+07:00", "(GMT+07:00)Bangkok,Hanoi,Jakarta");
		table.put("GMT+07:00", "(GMT+07:00)Krasnoyarsk");
		table.put("GMT+08:00", "(GMT+08:00)Beijing,Chongqing,HongKong,Urumqi");
		table.put("GMT+08:00", "(GMT+08:00)Irkutsk,Ulaan,Bataar");
		table.put("GMT+08:00", "(GMT+08:00)KualaLumpur,Singapore");
		table.put("GMT+08:00", "(GMT+08:00)Perth");
		table.put("GMT+08:00", "(GMT+08:00)Taipei");
		table.put("GMT+09:00", "(GMT+09:00)Osaka,Sapporo,Tokyo");
		table.put("GMT+09:00", "(GMT+09:00)Seoul");
		table.put("GMT+09:00", "(GMT+09:00)Yakutsk");
		table.put("GMT+09:30", "(GMT+09:30)Adelaide");
		table.put("GMT+09:30", "(GMT+09:30)Darwin");
		table.put("GMT+10:00", "(GMT+10:00)Brisbane");
		table.put("GMT+10:00", "(GMT+10:00)Canberra,Melbourne,Sydney");
		table.put("GMT+10:00", "(GMT+10:00)Guam,PortMoresby");
		table.put("GMT+10:00", "(GMT+10:00)Hobert");
		table.put("GMT+10:00", "(GMT+10:00)Viadivostok");
		table.put("GMT+11:00", "(GMT+11:00)Magadan,SolomanIs.,NewCaledonia");
		table.put("GMT+12:00", "(GMT+12:00)Auckland,Wellington");
		table.put("GMT+12:00", "(GMT+12:00)Fiji,Kamchatka,MarshallIs.");
		table.put("GMT+13:00", "(GMT+13:00)Nuku'alofa");

		Enumeration e = table.keys();

		if (table.containsKey(key)) {
			while (e.hasMoreElements()) {
				key1 = (String) e.nextElement();
				if (key1.equalsIgnoreCase(key)) {
					gmtValue = (String) table.get(key);
				}
			}
		}
		return gmtValue;
	}
}
