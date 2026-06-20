package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.AnalyticsInsightsResponse.Status;
import br.com.concurseiro.api.analytics.dto.AnalyticsInsightsResponse.Confidence;
import br.com.concurseiro.api.analytics.service.AnalyticsHealthAnalyzer.Snapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsHealthAnalyzerTest {
    private final AnalyticsHealthAnalyzer analyzer = new AnalyticsHealthAnalyzer();

    @Test void appIndoBem() {
        var result = analyzer.analyze(snapshot(2000,100,80,90,80,75,120,900,420,30,15,8,5,0,2,200), snapshot(1500,80,55,75,60,50,100,600,360,25,10,5,8,2,5,150));
        assertEquals(Status.GOOD, result.status()); assertTrue(result.score() >= 70);
    }
    @Test void appIndoMal() {
        var result = analyzer.analyze(snapshot(1000,100,6,90,20,5,180,12,25,0,0,0,30,20,15,600), snapshot(900,90,8,80,22,7,150,14,40,2,1,0,20,10,10,500));
        assertEquals(Status.BAD, result.status()); assertTrue(result.score() < 45);
    }
    @Test void appEstavel() {
        Snapshot current=snapshot(500,40,20,35,25,18,45,100,190,18,7,3,5,2,1,90);
        var result=analyzer.analyze(current,current); assertEquals(Status.STABLE,result.status()); assertEquals(0,result.scoreChangePercent());
    }
    @Test void dadosInsuficientes() {
        var result=analyzer.analyze(snapshot(20,3,1,3,1,1,4,2,100,0,0,0,0,0,0,5),snapshot(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0));
        assertEquals(Status.INSUFFICIENT_DATA,result.status()); assertEquals(Confidence.LOW,result.confidence());
    }
    @Test void quedaDeErrosEhPositiva() {
        var result=analyzer.analyze(snapshot(500,30,15,25,18,14,40,80,200,15,6,2,3,1,2,80),snapshot(500,30,15,25,18,14,40,80,200,15,6,2,3,10,2,80));
        assertTrue(result.drivers().stream().anyMatch(d->d.metric().equals("errorsRecent")&&d.type().equals("positive")));
    }
    @Test void detectaTrafegoAutomatizado() {
        var result=analyzer.analyze(snapshot(1000,10,5,10,5,3,100,3,20,0,0,0,0,0,0,800),snapshot(500,8,4,8,4,2,60,2,20,0,0,0,0,0,0,400));
        assertTrue(result.possibleAutomatedTraffic());
    }

    private Snapshot snapshot(long events,long active,long real,long opened,long viewed,long first,long sessions,long questions,double duration,double d1,double d7,double d30,long noResult,long errors,long unknown,long screens){
        return new Snapshot(events,active,real,opened,viewed,first,sessions,questions,duration,d1,d7,d30,Math.max(noResult*4,1),noResult,10,0,unknown,0,0,errors,0,screens);
    }
}
