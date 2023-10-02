package demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "com.demo")
@Component
public class ConfigProperties {

    private String filesURL;
    private List<String> rankingwords;

    public String getFilesURL() {
        return filesURL;
    }

    public void setFilesURL(String URL) {
        this.filesURL = URL;
    }

    public List<String> getRankingWords() {
        return rankingwords;
    }

    public void setRankingWords(List<String> rankingwords) {
        this.rankingwords = rankingwords;
    }
}
