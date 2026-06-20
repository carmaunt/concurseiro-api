package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.AnalyticsInsightsResponse;
import br.com.concurseiro.api.analytics.dto.AnalyticsInsightsResponse.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AnalyticsHealthAnalyzer {
    public static final int MIN_EVENTS = 50, MIN_REAL_ACTIVE = 5, MIN_QUESTIONS = 10;
    public enum Direction { UP_IS_GOOD, DOWN_IS_GOOD }
    public static final Map<String, Direction> METRIC_DIRECTION = Map.ofEntries(
            Map.entry("activeUsers", Direction.UP_IS_GOOD), Map.entry("realActiveUsers", Direction.UP_IS_GOOD),
            Map.entry("activationRate", Direction.UP_IS_GOOD), Map.entry("questionsAnswered", Direction.UP_IS_GOOD),
            Map.entry("questionsPerSession", Direction.UP_IS_GOOD), Map.entry("questionsPerRealActiveUser", Direction.UP_IS_GOOD),
            Map.entry("averageSessionSeconds", Direction.UP_IS_GOOD), Map.entry("retentionD1", Direction.UP_IS_GOOD),
            Map.entry("retentionD7", Direction.UP_IS_GOOD), Map.entry("retentionD30", Direction.UP_IS_GOOD),
            Map.entry("filtersWithoutResult", Direction.DOWN_IS_GOOD), Map.entry("searchesWithoutResult", Direction.DOWN_IS_GOOD),
            Map.entry("unknownEvents", Direction.DOWN_IS_GOOD), Map.entry("missingSessionPercent", Direction.DOWN_IS_GOOD),
            Map.entry("missingIdentityPercent", Direction.DOWN_IS_GOOD), Map.entry("errorsRecent", Direction.DOWN_IS_GOOD),
            Map.entry("inactiveUsers", Direction.DOWN_IS_GOOD));

    public Analysis analyze(Snapshot current, Snapshot previous) {
        Derived c = derived(current), p = derived(previous);
        int score = score(c), previousScore = score(p);
        boolean automated = (c.sessionsPerActive > 4 && c.questionsPerSession < .5)
                || (c.averageSessionSeconds < 60 && c.sessions >= 10)
                || (c.screenViews > Math.max(c.questions * 10, 50) && c.sessions >= 10);
        String automatedReason = automated ? "Muitas sessões/telas curtas para poucas questões respondidas; o tráfego pode conter testes automatizados." : null;
        boolean insufficient = c.events < MIN_EVENTS || c.realActive < MIN_REAL_ACTIVE || c.questions < MIN_QUESTIONS;
        boolean criticalQuality = c.missingSessionPercent > 20 || c.missingIdentityPercent > 20 || c.unknownRate > 5;
        Double scoreChange = change(score, previousScore);
        Status status = insufficient ? Status.INSUFFICIENT_DATA
                : score >= 70 && !criticalQuality && (scoreChange == null || scoreChange >= -5) ? Status.GOOD
                : score < 45 || criticalQuality ? Status.BAD : Status.STABLE;
        Confidence confidence = c.realActive >= 50 && c.questions >= 200 && c.events >= 1000 && c.missingSessionPercent < 5 && c.missingIdentityPercent < 5 && !automated ? Confidence.HIGH
                : c.realActive >= 10 && c.questions >= 50 && c.events >= 200 && !automated ? Confidence.MEDIUM : Confidence.LOW;
        String confidenceReason = confidence == Confidence.HIGH ? "Amostra ampla e tracking consistente."
                : confidence == Confidence.MEDIUM ? "Amostra útil, ainda limitada para segmentações menores."
                : automated ? "Amostra pequena ou com sinais de tráfego de teste."
                : "Poucos eventos, usuários reais ou questões respondidas no período.";

        Map<String, MetricInsight> metrics = new LinkedHashMap<>();
        metric(metrics,"activeUsers",c.active,p.active,"Mais pessoas usando o app.");
        metric(metrics,"realActiveUsers",c.realActive,p.realActive,"Usuários que responderam ao menos uma questão.");
        metric(metrics,"activationRate",c.activationRate,p.activationRate,"Conversão da abertura até a primeira resposta.");
        metric(metrics,"questionsAnswered",c.questions,p.questions,"Volume de estudo no período.");
        metric(metrics,"questionsPerSession",c.questionsPerSession,p.questionsPerSession,"Questões respondidas em cada sessão.");
        metric(metrics,"questionsPerRealActiveUser",c.questionsPerRealActive,p.questionsPerRealActive,"Profundidade de estudo por usuário real.");
        metric(metrics,"averageSessionSeconds",c.averageSessionSeconds,p.averageSessionSeconds,"Tempo médio produtivo no app.");
        metric(metrics,"retentionD1",c.retentionD1,p.retentionD1,"Retorno no dia seguinte.");
        metric(metrics,"filtersWithoutResult",c.filtersWithoutResult,p.filtersWithoutResult,"Filtros que não encontraram conteúdo.");
        metric(metrics,"errorsRecent",c.errors,p.errors,"Erros registrados no período.");

        List<Driver> drivers = drivers(c,p,insufficient,automated);
        List<Recommendation> recommendations = recommendations(c,insufficient,automated);
        String title = switch(status) { case GOOD -> "O app apresenta boa saúde"; case BAD -> "O app exige atenção"; case STABLE -> "O app está estável"; case INSUFFICIENT_DATA -> "Ainda há poucos dados reais"; };
        String summary = switch(status) {
            case GOOD -> "Ativação e engajamento estão saudáveis no período analisado.";
            case BAD -> "Há baixo engajamento ou problemas de qualidade que precisam ser investigados.";
            case STABLE -> "Não há uma tendência dominante; existem sinais positivos e pontos de atenção.";
            case INSUFFICIENT_DATA -> "O tracking pode ser validado tecnicamente, mas a amostra ainda não sustenta decisões fortes de produto.";
        };
        return new Analysis(status,score,previousScore,scoreChange,confidence,confidenceReason,title,summary,automated,automatedReason,metrics,drivers,recommendations);
    }

    private Derived derived(Snapshot s) {
        double activation = s.opened > 0 ? pct(s.firstAnswered,s.opened) : pct(s.realActive,s.active);
        return new Derived(s.events,s.active,s.realActive,s.sessions,s.questions,s.averageSessionSeconds,s.retentionD1,s.retentionD7,s.retentionD30,
                s.filtersApplied,s.filtersWithoutResult,s.searches,s.searchesWithoutResult,s.unknownEvents,s.missingSessionPercent,s.missingIdentityPercent,
                s.errors,s.inactiveUsers,s.screenViews,activation,ratio(s.questions,s.sessions),ratio(s.questions,s.realActive),ratio(s.sessions,s.active),pct(s.unknownEvents,s.events));
    }
    private int score(Derived d) {
        double activation = scale(d.activationRate,20,75);
        double productiveDuration = d.questions == 0 ? 0 : scale(d.averageSessionSeconds,60,600);
        double engagement = .4*scale(d.questionsPerSession,.5,10)+.3*scale(d.questionsPerRealActive,1,20)+.3*productiveDuration;
        double retention = .55*scale(d.retentionD1,5,25)+.3*scale(d.retentionD7,2,10)+.15*scale(d.retentionD30,1,8);
        double filterRate = d.filtersApplied == 0 ? 0 : pct(d.filtersWithoutResult,d.filtersApplied);
        double searchRate = d.searches == 0 ? 0 : pct(d.searchesWithoutResult,d.searches);
        double content = clamp(100-filterRate*1.5-searchRate);
        double quality = clamp(100-d.missingSessionPercent*2-d.missingIdentityPercent*2-d.unknownRate*3-Math.min(d.errors*3,30));
        return (int)Math.round(clamp(.30*activation+.30*engagement+.20*retention+.10*content+.10*quality));
    }
    private void metric(Map<String,MetricInsight> out,String name,double c,double p,String text){Double ch=change(c,p);out.put(name,new MetricInsight(round(c),round(p),ch,trend(ch),text));}
    private List<Driver> drivers(Derived c,Derived p,boolean insufficient,boolean automated){List<Driver>d=new ArrayList<>();
        if(insufficient)d.add(driver("warning","Amostra pequena demais para conclusão","Ainda não há volume mínimo de eventos, usuários reais e respostas.","sampleSize",c.events,p.events,change(c.events,p.events),"high"));
        addChangeDriver(d,"activationRate",c.activationRate,p.activationRate,"Taxa de ativação","Mais usuários chegaram à primeira resposta.",false);
        addChangeDriver(d,"questionsAnswered",c.questions,p.questions,"Questões respondidas","O volume de estudo mudou no período.",false);
        if(c.questionsPerSession<.5)d.add(driver("negative","Questões por sessão está baixo","Há muitas sessões para poucas respostas.","questionsPerSession",round(c.questionsPerSession),round(p.questionsPerSession),change(c.questionsPerSession,p.questionsPerSession),"high"));
        if(c.missingSessionPercent<5&&c.missingIdentityPercent<5)d.add(driver("positive","Tracking consistente","Eventos possuem sessão e identidade opaca.","dataQuality",round(c.missingSessionPercent),round(p.missingSessionPercent),change(c.missingSessionPercent,p.missingSessionPercent),"low"));
        if(c.filtersWithoutResult>0)addChangeDriver(d,"filtersWithoutResult",c.filtersWithoutResult,p.filtersWithoutResult,"Filtros sem resultado","Usuários podem estar procurando conteúdo ausente.",true);
        if(c.errors>0)addChangeDriver(d,"errorsRecent",c.errors,p.errors,"Erros recentes","Erros afetam a experiência e o score.",true);
        if(automated)d.add(driver("warning","Possível tráfego automatizado/teste","Sessões e telas não estão convertendo em estudo.","possibleAutomatedTraffic",true,false,null,"high"));
        return d.stream().limit(5).toList();}
    private void addChangeDriver(List<Driver>d,String metric,double c,double p,String title,String desc,boolean downGood){Double ch=change(c,p);if(ch==null)return;boolean positive=downGood?ch<-5:ch>5;boolean negative=downGood?ch>5:ch<-5;d.add(driver(positive?"positive":negative?"negative":"neutral",title+(positive?" melhorou":negative?" piorou":" está estável"),desc,metric,round(c),round(p),ch,negative?"high":"medium"));}
    private List<Recommendation> recommendations(Derived c,boolean insufficient,boolean automated){List<Recommendation>r=new ArrayList<>();if(c.activationRate<40)r.add(rec("high","Melhorar caminho até a primeira questão","Reduza a fricção inicial e destaque uma ação clara para começar a resolver.","activationRate"));if(c.questionsPerSession<1.5)r.add(rec("high","Aumentar estudo por sessão","Revise se o usuário encontra rapidamente a próxima questão.","questionsPerSession"));if(c.filtersWithoutResult>0)r.add(rec("medium","Revisar catálogo","Mapeie filtros sem resultado e priorize os conteúdos procurados.","filtersWithoutResult"));if(c.retentionD1<15&&!insufficient)r.add(rec("medium","Criar incentivo de retorno","Use progresso, meta diária e continuar de onde parou.","retentionD1"));if(automated)r.add(rec("medium","Separar tráfego real de testes","Prepare environment/installSource para excluir Test Lab e desenvolvimento.","possibleAutomatedTraffic"));if(insufficient)r.add(rec("low","Aguardar mais uso real","Não tome decisões fortes antes de alcançar a amostra mínima configurada.","sampleSize"));return r.stream().limit(4).toList();}
    private Driver driver(String type,String title,String desc,String metric,Object c,Object p,Double ch,String severity){return new Driver(type,title,desc,metric,c,p,ch,severity);} private Recommendation rec(String p,String t,String d,String m){return new Recommendation(p,t,d,m);}
    private Trend trend(Double c){return c==null?Trend.NO_BASE:c>5?Trend.UP:c< -5?Trend.DOWN:Trend.STABLE;}
    static Double change(double c,double p){return p==0?null:round((c-p)/Math.abs(p)*100);} static double ratio(double a,double b){return b==0?0:a/b;} static double pct(double a,double b){return ratio(a,b)*100;} static double scale(double x,double low,double high){return clamp((x-low)/(high-low)*100);} static double clamp(double x){return Math.max(0,Math.min(100,x));} static double round(double x){return Math.round(x*10.0)/10.0;}

    public record Snapshot(long events,long active,long realActive,long opened,long firstViewed,long firstAnswered,long sessions,long questions,double averageSessionSeconds,double retentionD1,double retentionD7,double retentionD30,long filtersApplied,long filtersWithoutResult,long searches,long searchesWithoutResult,long unknownEvents,double missingSessionPercent,double missingIdentityPercent,long errors,long inactiveUsers,long screenViews){}
    private record Derived(long events,long active,long realActive,long sessions,long questions,double averageSessionSeconds,double retentionD1,double retentionD7,double retentionD30,long filtersApplied,long filtersWithoutResult,long searches,long searchesWithoutResult,long unknownEvents,double missingSessionPercent,double missingIdentityPercent,long errors,long inactiveUsers,long screenViews,double activationRate,double questionsPerSession,double questionsPerRealActive,double sessionsPerActive,double unknownRate){}
    public record Analysis(Status status,int score,int previousScore,Double scoreChangePercent,Confidence confidence,String confidenceReason,String title,String summary,boolean possibleAutomatedTraffic,String automatedTrafficReason,Map<String,MetricInsight> metrics,List<Driver> drivers,List<Recommendation> recommendations){}
}
