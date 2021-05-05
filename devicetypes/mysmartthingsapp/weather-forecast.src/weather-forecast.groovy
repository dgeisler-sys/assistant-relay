/*
 *  Weather Forecast
 *
 *  Author: MSEGS
 *  V3.0
 *  Â© 2019, 2020, 2021
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  3.0 | 2021-02-27: Added - (1) Icons | (2) Set 3 additional weather condition locations
 *  2.3 | 2021-02-15: Added - (1) Day 4, 5 & 6 to forecast / Additional attributes (current conditions) | (2) Option to set a custom 'default'(no alert) alert message | (3) Option to set a Precip forecast day (Today, Tomorrow, Following day)
 *  2.2 | 2021-02-07: Fixed - Days High [0] would report null at appx 15:00
 *  2.1 | 2021-02-05: Fixed - Precip typo error
 *  1.0 | 2019-03-24
 */
metadata {
    definition (name: "Weather Forecast", namespace: "mysmartthingsapp", author: "MSEGS") {
	capability "Temperature Measurement"
	capability "Illuminance Measurement"
	capability "Relative Humidity Measurement"
	capability "Ultraviolet Index"
	capability "Refresh"

	attribute "lastUpdate", "string"        
	attribute "localSunrise", "string"
	attribute "localSunset", "string"
	attribute "city", "string"
	attribute "weather", "string"
	attribute "illuminance", "string"
	attribute "ultravioletIndex", "string"
	attribute "uvDescription", "string"
	attribute "cloudCover", "number"
	attribute "cloudCeiling", "string"
	attribute "wind", "number"
	attribute "windVector", "string"
	attribute "visibility", "number"    
	attribute "moonPhase", "string"
	attribute "weatherIcon", "string"
	attribute "currentIcon", "string"
	attribute "todayIcon", "string"
	attribute "tomorrowIcon", "string"
	attribute "day2Icon", "string"
	attribute "day3Icon", "string"
	attribute "day4Icon", "string"
	attribute "day5Icon", "string"
	attribute "day6Icon", "string"
	attribute "feelsLike", "number"
	attribute "percentPrecip", "number"
	attribute "dewPoint", "number"
	attribute "pressure", "string"
	attribute "rain", "string"    
	attribute "forecastForRain", "string"
	attribute "precipType", "string"
	attribute "qpf", "number"    
	attribute "precipType+QPF", "string"    
	attribute "snowRange", "string"
	attribute "alert", "string"
	attribute "todaySummary", "string"
	attribute "tomorrowSummary", "string"
	attribute "seconddaySummary", "string"
	attribute "thirddaySummary", "string"
	attribute "fourthdaySummary", "string"
	attribute "fifthdaySummary", "string"
	attribute "sixthdaySummary", "string"
	attribute "todayShort", "string"
	attribute "todayLong", "string"
	attribute "tomorrowShort", "string"
	attribute "tomorrowLong", "string"
	attribute "day2Short", "string"
	attribute "day2Long", "string"
	attribute "day3Short", "string"
	attribute "day3Long", "string"
	attribute "day4Short", "string"
	attribute "day4Long", "string"
	attribute "day5Short", "string"
	attribute "day5Long", "string"
	attribute "day6Short", "string"
	attribute "day6Long", "string"
	attribute "todayHigh", "number"
	attribute "todayLow", "number"
	attribute "tomorrowHigh", "number"
	attribute "tomorrowLow", "number"
	attribute "seconddayHigh", "number"
	attribute "seconddayLow", "number"
	attribute "todayPH", "string"
	attribute "tomorrowPH", "string"
	attribute "day2PH", "string"
	attribute "day3PH", "string"
	attribute "day4PH", "string"
	attribute "day5PH", "string"
	attribute "day6PH", "string"
	attribute "location1", "string"
	attribute "location2", "string"
	attribute "location3", "string"
}

    preferences {
        input "zipCode", "text", title: "Zip Code (optional)", required: false
    	input "frequency", "number", title: "Update this many minutes:", description: "Minutes", required: false //defaultValue: 15
    	input "showMeasure", "bool", title: "Show measure units", description: "C/F, KPH/MPH ...", required: false
    	input "showDegreeSymbol", "bool", title: "Show measure symbols", description: "Â°, %, â†‘â†“", required: false
        input "weatherDisplay", "enum", title: "Display weather forecast as", options: ["0":"Icon only", "1":"Phrase only", "2":"Both"], description: "Display option", required: false 
    	input "forecastDay", "enum", title: "Check for precipitation on this day", options: ["0":"Today", "2":"Tomorrow", "4":"Following Day"], description: "Forecast day", required: false
        input "showDay", "bool", title: "Show precipitation day", description: "Include forecast day", required: false
        input "defaultAlertMessage", "text", title: "Default alert message", description: "Message to display when there are no alerts", required: false
    	input "zipCode1", "text", title: "Additional Location (zip code)", description: "New York City", required: false
        input "zipCode2", "text", title: "Additional Location (zip code)", description: "Los Angeles", required: false
        input "zipCode3", "text", title: "Additional Location (zip code)", description: "Denver", required: false
    }
}

def parse(String description) {
    log.debug "Parsing '${description}'"
}

def installed() {
    initialize()
}

def uninstalled() {
    unschedule()
}

def updated(){
    log.debug "updated()"
    initialize()
}

def initialize(){
    poll()
    if(frequency){
		schedule("0 0/$frequency * ? * * *", poll)
	}else{
		runEvery15Minutes(poll)
	}
}

def poll() {

    def tempScale, windUnits, distanceUnits, qpfUnits, ceilingUnits, percentSymbol, tempSymbol, pressureSymbolRise, pressureSymbolFall 
    if (showMeasure){
    	tempScale = getTemperatureScale()
    	windUnits = tempScale == "C" ? "KPH" : "MPH"
    	distanceUnits = tempScale == "C" ? "KM" : "MI"
    	qpfUnits = tempScale == "C" ? "CM" : "IN"
        ceilingUnits = tempScale == "C" ? "M" : "FT"
    }else{
    	tempScale = ""
        windUnits = ""
    	distanceUnits = ""
    	qpfUnits = ""
        ceilingUnits = ""
	}    
    if(showDegreeSymbol){
        tempSymbol = "Â° "
	percentSymbol = "%"
        pressureSymbolRise = "â†‘"
	pressureSymbolFall = "â†“"
    }else{
    	tempSymbol = ""
	percentSymbol = ""
        pressureSymbolRise = ""
	pressureSymbolFall = ""
	}
    
    def timeZone = location.timeZone ?: timeZone(timeOfDay)
    def timeStamp = new Date().format("MMM dd EEE h:mm a", location.timeZone)
    sendEvent(name: "lastUpdate", value: timeStamp, displayed: false)

    def location1,location2,location3
    if(zipCode1){location1 = zipCode1}else{location1 = "10001"}//NY 
    if(zipCode2){location2 = zipCode2}else{location2 = "90002"}//LA
    if(zipCode3){location3 = zipCode3}else{location3 = "80201"}//DEN
    def obs1 = getTwcConditions(location1)
    def obs2 = getTwcConditions(location2)
    def obs3 = getTwcConditions(location3)
    if (obs1) {
    def loc1 = getTwcLocation(location1).location
    def city1NameTemp = "${loc1.city} ${obs1.temperature}${tempSymbol}${tempScale} ${icons(obs1.iconCode)}"
    send(name: "location1", value: city1NameTemp as String, displayed: false)
    }
    if (obs2) {
    def loc2 = getTwcLocation(location2).location
    def city2NameTemp = "${loc2.city} ${obs2.temperature}${tempSymbol}${tempScale} ${icons(obs2.iconCode)}"
    send(name: "location2", value: city2NameTemp as String, displayed: false)
    }
    if (obs3) {
    def loc3 = getTwcLocation(location3).location
    def city3NameTemp = "${loc3.city} ${obs3.temperature}${tempSymbol}${tempScale} ${icons(obs3.iconCode)}"
    send(name: "location3", value: city3NameTemp as String, displayed: false)
    }
    def obs = getTwcConditions(zipCode)
    if (obs) {
        def windVector = "${obs.windDirectionCardinal} ${obs.windSpeed}"
        def pressureTrend = obs.pressureTendencyCode
        def pressureSymbol
        if(pressureTrend == 1){
		pressureSymbol = pressureSymbolRise
        }else if(pressureTrend == 2){
        	pressureSymbol = pressureSymbolFall
		}else{
        	pressureSymbol = ""}
        def pressure = "${pressureSymbol}${obs.pressureAltimeter}"
        send(name: "weatherIcon", value: obs.iconCode as String, displayed: false)
        send(name: "currentIcon", value: "${icons(obs.iconCode)}" as String, displayed: false)
        send(name: "temperature", value: obs.temperature, unit: tempSymbol+tempScale, displayed: true, isStateChange: true)
        send(name: "feelsLike", value: obs.temperatureFeelsLike, unit: tempSymbol+tempScale, displayed: false, isStateChange: true)
        send(name: "humidity", value: obs.relativeHumidity, unit: percentSymbol, displayed: false, isStateChange: true)
        send(name: "dewPoint", value: obs.temperatureDewPoint, unit: tempSymbol, displayed: false, isStateChange: true)
        send(name: "pressure", value: pressure, unit: qpfUnits, displayed: false, isStateChange: true)
        send(name: "weather", value: obs.wxPhraseLong, displayed: false)
        send(name: "cloudCeiling", value: obs.cloudCeiling, unit: ceilingUnits, displayed: false, isStateChange: true)
        send(name: "wind", value: obs.windSpeed, unit: windUnits, displayed: false, isStateChange: true)
        send(name: "windVector", value: windVector, unit: windUnits, displayed: false, isStateChange: true)
        send(name: "visibility", value: obs.visibility, unit: distanceUnits, displayed: false, isStateChange: true)
        send(name: "ultravioletIndex", value: obs.uvIndex, displayed: false)
        send(name: "uvDescription", value: obs.uvDescription, displayed: false)

	log.trace "Getting location info"
        def loc = getTwcLocation(zipCode).location
        def cityValue = "${loc.city}, ${loc.adminDistrictCode} ${loc.countryCode}"
        if (cityValue != device.currentValue("city")) {
            send(name: "city", value: cityValue, isStateChange: true, displayed: false)
        }

        def dtf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        def sunriseDate = dtf.parse(obs.sunriseTimeLocal)
        def sunsetDate = dtf.parse(obs.sunsetTimeLocal)

        def tf = new java.text.SimpleDateFormat("h:mm a")
        tf.setTimeZone(TimeZone.getTimeZone(loc.ianaTimeZone))

        def localSunrise = "${tf.format(sunriseDate)}"
        def localSunset = "${tf.format(sunsetDate)}"
        send(name: "localSunrise", value: localSunrise, isStateChange: true, descriptionText: "Sunrise today is at $localSunrise")
        send(name: "localSunset", value: localSunset, isStateChange: true, descriptionText: "Sunset today at is $localSunset")

        send(name: "illuminance", value: estimateLux(obs, sunriseDate, sunsetDate), displayed: false)

        // Forecast
        def f = getTwcForecast(zipCode)
        if (f) {
    
            def icon, imagePath, todayIcon, dayHigh, cloudCover, precipType, precipType_QPF, precipChance, snowRange, narrative, forecastTodayShort, forecastTodayLong, todayPH
            icon = f.daypart[0].iconCode[0] ?: f.daypart[0].iconCode[1]
            precipChance = f.daypart[0].precipChance[0] ?: f.daypart[0].precipChance[1]
            
            def precipTypeToday = "${f.daypart[0].precipType[0]}"
            precipTypeToday = precipTypeToday.replace("precip","N/A")
            def precipDescToday = precipTypeToday[0].toUpperCase() + precipTypeToday.substring(1)
            def precipTypeTonight = "${f.daypart[0].precipType[1]}"
            precipTypeTonight = precipTypeTonight.replace("precip","N/A")
            def precipDescTonight = precipTypeTonight[0].toUpperCase() + precipTypeTonight.substring(1)
            
            def precipTypeTom = "${f.daypart[0].precipType[2]}"
            precipTypeTom = precipTypeTom.replace("precip","N/A")
            def precipDescTom = precipTypeTom[0].toUpperCase() + precipTypeTom.substring(1)
            
            def precipTypeDay2 = "${f.daypart[0].precipType[4]}"
            precipTypeDay2 = precipTypeDay2.replace("precip","N/A")
            def precipDescDay2 = precipTypeDay2[0].toUpperCase() + precipTypeDay2.substring(1)
            
            def precipTypeDay3 = "${f.daypart[0].precipType[6]}"
            precipTypeDay3 = precipTypeDay3.replace("precip","N/A")
            def precipDescDay3 = precipTypeDay3[0].toUpperCase() + precipTypeDay3.substring(1)

            def precipTypeDay4 = "${f.daypart[0].precipType[8]}"
            precipTypeDay4 = precipTypeDay4.replace("precip","N/A")
            def precipDescDay4 = precipTypeDay4[0].toUpperCase() + precipTypeDay4.substring(1)
            
            def precipTypeDay5 = "${f.daypart[0].precipType[10]}"
            precipTypeDay5 = precipTypeDay5.replace("precip","N/A")
            def precipDescDay5 = precipTypeDay5[0].toUpperCase() + precipTypeDay5.substring(1)
            
            def precipTypeDay6 = "${f.daypart[0].precipType[12]}"
            precipTypeDay6 = precipTypeDay6.replace("precip","N/A")
            def precipDescDay6 = precipTypeDay6[0].toUpperCase() + precipTypeDay6.substring(1)
			
            def weatherCondition = obs.wxPhraseLong
			def forecast4Rain
            def filterList = ["rain","thunder"]
			def isRaining = false
            def forecastRain = false
            
            if(timeOfDayIsBetween("03:00", "15:00", new Date(), location.timeZone)){
            todayIcon = "${icons(f.daypart[0].iconCode[0])}"
            narrative = "Today\n${f.daypart[0].narrative[0]}"
            dayHigh = "${f.temperatureMax[0]} $tempSymbol$tempScale"
            cloudCover = "${f.daypart[0].cloudCover[0]}"
            forecastTodayShort = "${renderWeather(f,0,0,0,0,"wxshort")}"
            forecastTodayLong = "${renderWeather(f,0,0,0,0,"wxlong")}"
            todayPH = "Humidity: ${f.daypart[0].relativeHumidity[0]}$percentSymbol, Precip: ${f.daypart[0].precipChance[0]}$percentSymbol - ${precipDescToday}"
            }else{
            todayIcon = "${icons(f.daypart[0].iconCode[1])}"
            narrative = "Tonight\n${f.daypart[0].narrative[1]}"
            dayHigh = "${f.temperatureMax[1]} $tempSymbol$tempScale"
            cloudCover = "${f.daypart[0].cloudCover[1]}"
            forecastTodayShort = "${renderWeather(f,0,1,0,1,"wxshort")}"
            forecastTodayLong = "${renderWeather(f,0,1,0,1,"wxlong")}"
            todayPH = "Humidity: ${f.daypart[0].relativeHumidity[1]}$percentSymbol, Precip: ${f.daypart[0].precipChance[1]}$percentSymbol - ${precipDescTonight}"
            }
            
            if(forecastDay == "2"){
            precipType = "${precipDay(f)} ${precipDescTom}"
            precipType_QPF = "${precipDay(f)} ${precipDescTom} - ${qpf(f)}"
            snowRange = "${precipDay(f)} ${f.daypart[0].snowRange[2]}"
            forecast4Rain = f.daypart[0].narrative[2]
            }else if(forecastDay == "4"){
            precipType = "${precipDay(f)} ${precipDescDay2}"
            precipType_QPF = "${precipDay(f)} ${precipDescDay2} - ${qpf(f)}"
            snowRange = "${precipDay(f)} ${f.daypart[0].snowRange[4]}"
            forecast4Rain = f.daypart[0].narrative[4]
            }else{
            if(timeOfDayIsBetween("03:00", "15:00", new Date(), location.timeZone)){
            precipType = "${precipDay(f)} ${precipDescToday}"
            precipType_QPF = "${precipDay(f)} ${precipDescToday} - ${qpf(f)}"
            snowRange = "${precipDay(f)} ${f.daypart[0].snowRange[0]}"
            forecast4Rain = f.daypart[0].narrative[0]
            }else{
            precipType = "${precipDay(f)} ${precipDescTonight}"
            precipType_QPF = "${precipDay(f)} ${precipDescTonight} - ${qpf(f)}"
            snowRange = "${precipDay(f)} ${f.daypart[0].snowRange[1]}"
            forecast4Rain = f.daypart[0].narrative[1]
            }
			}
            filterList.each() { word ->
				if(weatherCondition.toLowerCase().contains(word)) {isRaining = true}
				if(forecast4Rain.toLowerCase().contains(word)) {forecastRain = true}
            }
            def tomorrowIcon = "${icons(f.daypart[0].iconCode[2])}"
            def day2Icon = "${icons(f.daypart[0].iconCode[4])}"
            def day3Icon = "${icons(f.daypart[0].iconCode[6])}"
            def day4Icon = "${icons(f.daypart[0].iconCode[8])}"
            def day5Icon = "${icons(f.daypart[0].iconCode[10])}"
            def day6Icon = "${icons(f.daypart[0].iconCode[12])}"
            def tomorrowForecast = "${f.dayOfWeek[1]}\n${f.narrative[1]}"
            def seconddayForecast = "${f.dayOfWeek[2]}\n${f.narrative[2]}"
            def thirddayForecast = "${f.dayOfWeek[3]}\n${f.narrative[3]}"
            def fourthdayForecast = "${f.dayOfWeek[4]}\n${f.narrative[4]}"
            def fifthdayForecast = "${f.dayOfWeek[5]}\n${f.narrative[5]}"
            def sixthdayForecast = "${f.dayOfWeek[6]}\n${f.narrative[6]}"
            def todayHigh = dayHigh
            def todayLow = "${f.temperatureMin[0]} $tempSymbol$tempScale"
            def tomorrowHigh = f.temperatureMax[1]
            def tomorrowLow = f.temperatureMin[1]
            def seconddayHigh = f.temperatureMax[2]
            def seconddayLow = f.temperatureMin[2]
            def tomorrowPH = "Humidity: ${f.daypart[0].relativeHumidity[2]}$percentSymbol, Precip: ${f.daypart[0].precipChance[2]}$percentSymbol - ${precipDescTom}"
            def seconddayPH = "Humidity: ${f.daypart[0].relativeHumidity[4]}$percentSymbol, Precip: ${f.daypart[0].precipChance[4]}$percentSymbol - ${precipDescDay2}"
            def thirddayPH = "Humidity: ${f.daypart[0].relativeHumidity[6]}$percentSymbol, Precip: ${f.daypart[0].precipChance[6]}$percentSymbol - ${precipDescDay3}"
            def fourthdayPH = "Humidity: ${f.daypart[0].relativeHumidity[8]}$percentSymbol, Precip: ${f.daypart[0].precipChance[8]}$percentSymbol - ${precipDescDay4}"
            def fifthdayPH = "Humidity: ${f.daypart[0].relativeHumidity[10]}$percentSymbol, Precip: ${f.daypart[0].precipChance[10]}$percentSymbol - ${precipDescDay5}"
            def sixthdayPH = "Humidity: ${f.daypart[0].relativeHumidity[12]}$percentSymbol, Precip: ${f.daypart[0].precipChance[12]}$percentSymbol - ${precipDescDay6}"
            def forecastTomShort = "${renderWeather(f,1,1,1,2,"wxshort")}"
            def forecastTomLong = "${renderWeather(f,1,1,1,2,"wxlong")}"
            def forecastDay2Short = "${renderWeather(f,2,2,2,4,"wxshort")}"
            def forecastDay2Long = "${renderWeather(f,2,2,2,4,"wxlong")}"
            def forecastDay3Short = "${renderWeather(f,3,3,3,6,"wxshort")}"
            def forecastDay3Long = "${renderWeather(f,3,3,3,6,"wxlong")}"
            def forecastDay4Short = "${renderWeather(f,4,4,4,8,"wxshort")}"
            def forecastDay4Long = "${renderWeather(f,4,4,4,8,"wxlong")}"
            def forecastDay5Short = "${renderWeather(f,5,5,5,10,"wxshort")}"
            def forecastDay5Long = "${renderWeather(f,5,5,5,10,"wxlong")}"
            def forecastDay6Short = "${renderWeather(f,6,6,6,12,"wxshort")}"
            def forecastDay6Long = "${renderWeather(f,6,6,6,12,"wxlong")}"
            send(name: "todayIcon", value: todayIcon as String, displayed: false, isStateChange: true)
            send(name: "tomorrowIcon", value: "${icons(f.daypart[0].iconCode[2])}" as String, displayed: false, isStateChange: true)
            send(name: "day2Icon", value: "${icons(f.daypart[0].iconCode[4])}" as String, displayed: false, isStateChange: true)
            send(name: "day3Icon", value: "${icons(f.daypart[0].iconCode[6])}" as String, displayed: false, isStateChange: true)
            send(name: "day4Icon", value: "${icons(f.daypart[0].iconCode[8])}" as String, displayed: false, isStateChange: true)
            send(name: "day5Icon", value: "${icons(f.daypart[0].iconCode[10])}" as String, displayed: false, isStateChange: true)
            send(name: "day6Icon", value: "${icons(f.daypart[0].iconCode[12])}" as String, displayed: false, isStateChange: true)
            send(name: "rain", value: isRaining, displayed: false)
            send(name: "forecastForRain", value: forecastRain, displayed: false)
            send(name: "cloudCover", value: cloudCover, unit: percentSymbol, displayed: false, isStateChange: true)
            send(name: "percentPrecip", value: precipChance, unit: percentSymbol, displayed: false, isStateChange: true)
            send(name: "precipType", value: precipType, displayed: false)
            send(name: "qpf", value: precipDay(f)+qpf(f), unit: qpfUnits, displayed: false, isStateChange: true)
            send(name: "precipType+QPF", value: precipType_QPF, unit: qpfUnits, displayed: false, isStateChange: true)
            send(name: "snowRange", value: snowRange, unit: qpfUnits, displayed: false, isStateChange: true)
            send(name: "moonPhase", value: f.moonPhase[0], displayed: false)
            send(name: "todaySummary", value: narrative)
            send(name: "tomorrowSummary", value: tomorrowForecast, displayed: false)
            send(name: "seconddaySummary", value: seconddayForecast, displayed: false)
            send(name: "thirddaySummary", value: thirddayForecast, displayed: false)
            send(name: "fourthdaySummary", value: fourthdayForecast, displayed: false)
            send(name: "fifthdaySummary", value: fifthdayForecast, displayed: false)
            send(name: "sixthdaySummary", value: sixthdayForecast, displayed: false)
            send(name: "todayShort", value: forecastTodayShort, displayed: false)
            send(name: "todayLong", value: forecastTodayLong, displayed: false)
            send(name: "tomorrowShort", value: forecastTomShort, displayed: false)
            send(name: "tomorrowLong", value: forecastTomLong, displayed: false)
            send(name: "day2Short", value: forecastDay2Short, displayed: false)
            send(name: "day2Long", value: forecastDay2Long, displayed: false)
            send(name: "day3Short", value: forecastDay3Short, displayed: false)
            send(name: "day3Long", value: forecastDay3Long, displayed: false)
            send(name: "day4Short", value: forecastDay4Short, displayed: false)
            send(name: "day4Long", value: forecastDay4Long, displayed: false)
            send(name: "day5Short", value: forecastDay5Short, displayed: false)
            send(name: "day5Long", value: forecastDay5Long, displayed: false)
            send(name: "day6Short", value: forecastDay6Short, displayed: false)
            send(name: "day6Long", value: forecastDay6Long, displayed: false)
            send(name: "todayHigh", value: todayHigh, displayed: false)
            send(name: "tomorrowHigh", value: tomorrowHigh, unit: tempSymbol+tempScale, displayed: false, isStateChange: true)
            send(name: "seconddayHigh", value: seconddayHigh, unit: tempSymbol+tempScale, displayed: false, isStateChange: true)
            send(name: "todayLow", value: todayLow, displayed: false)
            send(name: "tomorrowLow", value: tomorrowLow, unit: tempSymbol+tempScale, displayed: false, isStateChange: true)
            send(name: "seconddayLow", value: seconddayLow, unit: tempSymbol+tempScale, displayed: false, isStateChange: true)
            send(name: "todayPH", value: todayPH, displayed: false)
            send(name: "tomorrowPH", value: tomorrowPH, displayed: false)
            send(name: "day2PH", value: seconddayPH, displayed: false)
            send(name: "day3PH", value: thirddayPH, displayed: false)
            send(name: "day4PH", value: fourthdayPH, displayed: false)
            send(name: "day5PH", value: fifthdayPH, displayed: false)
            send(name: "day6PH", value: sixthdayPH, displayed: false)
        }
        else {
            log.warn "Forecast not found"
        }

        // Alerts
        def alerts = getTwcAlerts("${loc.latitude},${loc.longitude}")
        if (alerts) {
            alerts.each {alert ->
                def msg = alert.headlineText
                if (alert.effectiveTimeLocal && !msg.contains(" from ")) {
                    msg += " from ${parseAlertTime(alert.effectiveTimeLocal).format("E hh:mm a", TimeZone.getTimeZone(alert.effectiveTimeLocalTimeZone))}"
                }
                if (alert.expireTimeLocal && !msg.contains(" until ")) {
                    msg += " until ${parseAlertTime(alert.expireTimeLocal).format("E hh:mm a", TimeZone.getTimeZone(alert.expireTimeLocalTimeZone))}"
                }
                send(name: "alert", value: msg, descriptionText: msg)
            }
        }
        else {
            send(name: "alert", value: defaultAlertMessage, descriptionText: msg)
       }
    }
    else {
        log.warn "No response from TWC API"
    }
    
    return null
}

def parseAlertTime(s) {
    def dtf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    def s2 = s.replaceAll(/([0-9][0-9]):([0-9][0-9])$/,'$1$2')
    dtf.parse(s2)
}

def refresh() {
    poll()
}

def configure() {
    poll()
}

private localDate(timeZone) {
    def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
    df.setTimeZone(TimeZone.getTimeZone(timeZone))
    df.format(new Date())
}

private send(map) {
    log.debug "WSTATION: event: $map"
    sendEvent(map)
}

private estimateLux(obs, sunriseDate, sunsetDate) {
def lux = 0
    if (obs.dayOrNight == "N") {
        lux = 10
    }
    else {
        //day
        log.info "lux: ${obs.iconCode}"
        switch(obs.iconCode) {
            case "1": case "2": case "3": case "4":
                lux = 200
                break;
            case "5": case "6": case "7": case "8": case "9": case "10": case "11": case "12":
            case "13": case "14": case "15": case "16": case "17": case "18": case "19": case "20":
            case "21": case "22": case "23": case "24": case "25": case "26": case "35": case "39": case "40": case "42": case "43":
                lux = 1000
                break;
            case "28": case "30": case "38": case "41":
                lux = 2500
                break;
            case "34": case "37":
                lux = 7500
                break;
            default:
                //sunny, clear
                lux = 10000
        }

        //adjust for dusk/dawn
        def now = new Date().time
        def afterSunrise = now - sunriseDate.time
        def beforeSunset = sunsetDate.time - now
        def oneHour = 1000 * 60 * 60

        if(afterSunrise < oneHour) {
            //dawn
            lux = (long)(lux * (afterSunrise/oneHour))
        } else if (beforeSunset < oneHour) {
            //dusk
            lux = (long)(lux * (beforeSunset/oneHour))
        }
    }
    lux
}

private qpf(f){
def qpAmt = 0	
    if(forecastDay == "2"){
			if(f.daypart[0].precipType[2] == "snow"){
				qpAmt = f.daypart[0].qpfSnow[2]
        	}else{
        		qpAmt = f.daypart[0].qpf[2]
			}
    }else if(forecastDay == "4"){
			if(f.daypart[0].precipType[4] == "snow"){
				qpAmt = f.daypart[0].qpfSnow[4]
        	}else{
        		qpAmt = f.daypart[0].qpf[4]
			}
    }else{
    	if(timeOfDayIsBetween("03:00", "15:00", new Date(), location.timeZone)){
			if(f.daypart[0].precipType[0] == "snow"){
				qpAmt = f.daypart[0].qpfSnow[0]
        	}else{
        		qpAmt = f.daypart[0].qpf[0]
			}
    	}else{
			if(f.daypart[0].precipType[1] == "snow"){
				qpAmt = f.daypart[0].qpfSnow[1]
			}else{
        		qpAmt = f.daypart[0].qpf[1]
			}
    	}
    }
    qpAmt
}

private precipDay(f){
def precipDay = ""	
    if(showDay){
		if(forecastDay == "2"){
    		precipDay = "${f.dayOfWeek[1].toUpperCase().substring(0, 3)}\n"
    	}else if(forecastDay == "4"){
    		precipDay = "${f.dayOfWeek[2].toUpperCase().substring(0, 3)}\n"
    	}else{
            precipDay = "${f.dayOfWeek[0].toUpperCase().substring(0, 3)}\n"//0
    	}
    }
	precipDay
}

private renderWeather(f,wd,d,n,dp,wx){
def weather, day, tempSymbol, percentSymbol
	if(wd == 0){
       	if(timeOfDayIsBetween("03:00", "15:00", new Date(), location.timeZone)){
        day = "TODAY"
        }else{
        day = "TONIGHT"
        }
    }else{
		if(wx == "wxshort"){
        	day = "${f.dayOfWeek[wd].toUpperCase().substring(0, 3)}"
		}else{
        	day = "${f.dayOfWeek[wd].toUpperCase()}"
    	}
    }	
    if(showDegreeSymbol == true){
        tempSymbol = "Â° "
		percentSymbol = "%"
    }else{
    	tempSymbol = ""
		percentSymbol = ""
	}

	if(wx == "wxshort"){        
        if(weatherDisplay == "0"){
       		weather = "${day}\n ${icons(f.daypart[0].iconCode[dp])}\n\n ${f.temperatureMax[d]}   |   ${f.temperatureMin[n]}"
    	}else if(weatherDisplay == "2"){
			weather = "${day}\n ${icons(f.daypart[0].iconCode[dp])}\n ${f.daypart[0].wxPhraseShort[dp]}\n\n ${f.temperatureMax[d]}   |   ${f.temperatureMin[n]}"
		}else{//1 default
			weather = "${day}\n ${f.daypart[0].wxPhraseShort[dp]}\n\n ${f.temperatureMax[d]}   |   ${f.temperatureMin[n]}"
    	}
    }else if(wx == "wxlong"){
        if(weatherDisplay == "0"){
			weather = "${day}:\n\n ${icons(f.daypart[0].iconCode[dp])}\n\n High: ${f.temperatureMax[d]}$tempSymbol Low: ${f.temperatureMin[n]}$tempSymbol\n Humid: ${f.daypart[0].relativeHumidity[dp]}$percentSymbol\n Precip: ${f.daypart[0].precipChance[dp]}$percentSymbol"
    	}else if(weatherDisplay == "2"){
			weather = "${day}:\n\n ${icons(f.daypart[0].iconCode[dp])}\n${f.daypart[0].wxPhraseLong[dp]}\n\n High: ${f.temperatureMax[d]}$tempSymbol Low: ${f.temperatureMin[n]}$tempSymbol\n" 
            weather = weather + "Humid: ${f.daypart[0].relativeHumidity[dp]}$percentSymbol\n Precip: ${f.daypart[0].precipChance[dp]}$percentSymbol"
		}else{//1 default
			weather = "${day}\n\n ${f.daypart[0].wxPhraseLong[dp]}\n\n High: ${f.temperatureMax[d]}$tempSymbol Low: ${f.temperatureMin[n]}$tempSymbol\n Humid: ${f.daypart[0].relativeHumidity[dp]}$percentSymbol\n Precip: ${f.daypart[0].precipChance[dp]}$percentSymbol"
    	}
    }
    weather
}

private icons(code){
def icon
// *** WARNING: Do NOT change the icon symbols. Doing so may cause the device to stop updating ***
def sunny = "â˜€"
def cloudy = "â˜"
def pcloudy = "â›…"
def mcloudy = "â›…"
def rain = "â˜‚"
def thunder = "â›ˆ"
def thunder_rain = "â›ˆ"
def sun_rain = "â˜‚"
def snow = "â„"
def clear = "â˜¾"
def sleet = "â‹°â‹°"
def fog = "â˜"
def wind = "â‡¶"
def unknown = "âŠ"

switch(code) {
	case "1": case "2": case "44":
	icon = unknown
	break;
	case "3": case "4": case "47":
	icon = thunder_rain
	break;
	case "5": case "7": case "13": case "14": case "15": case "16": case "25": case "41": case "42": case "43": case "46":
	icon = snow
	break;
	case "6": case "8": case "9": case "10": case "11": case "12": case "35": case "39": case "40": case "45":
	icon = rain
	break;
	case "17": case "18":
	icon = sleet
	break;
	case "19": case "20": case "21": case "22":
	icon = fog
	break;
	case "23": case "24":
	icon = wind
	break;
	case "26": case "27": case "29":
	icon = cloudy
	break;
	case "28":
	icon = mcloudy
	break;
	case "30":
	icon = pcloudy
	break;
	case "31": case "33":
	icon = clear
	break;
	case "32": case "34": case "36":
	icon = sunny
	break;
	case "37": case "38":
	icon = sun_rain
	break;
	default:
	icon = unknown
}
icon
}