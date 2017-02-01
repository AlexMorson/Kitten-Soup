package alexmorson.kittensoup;

import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hack to get around doing network operations in the main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        try {
            gridView.setAdapter(new GridAdapter(this, getImageUrls()));
        } catch (IOException e) {

        } catch (ParserConfigurationException e) {

        } catch (SAXException e) {

        }
    }

    public class GridAdapter extends ArrayAdapter {
        private Context context;
        private LayoutInflater inflater;
        private ArrayList<String> urls;

        public GridAdapter(Context context, ArrayList<String> urls) {
            super(context, R.layout.grid_view_image, urls);

            this.context = context;
            this.urls = urls;

            this.inflater = LayoutInflater.from(this.context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.grid_view_image, parent, false);
            }

            Picasso
                .with(this.context)
                .load(urls.get(position))
                .resize(75, 75)
                .into((ImageView) convertView);

            return convertView;
        }
    }

    public ArrayList<String> getImageUrls() throws IOException, ParserConfigurationException, SAXException {
        URL infoUrl = new URL("https://api.flickr.com/services/rest/?&method=flickr.photos.search&api_key=4ef2fe2affcdd6e13218f5ddd0e2500d&tags=kitten&per_page=500");
        BufferedReader in = new BufferedReader(new InputStreamReader(infoUrl.openStream(), "UTF-8"));

        StringBuilder response = new StringBuilder();
        String currentLine;
        while ((currentLine = in.readLine()) != null) {
            response.append(currentLine);
        }
        String xml = response.toString();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();

        NodeList photos = ((Element) rootElement.getElementsByTagName("photos").item(0))
                                                .getElementsByTagName("photo");

        ArrayList<String> urls = new ArrayList<>();

        for (int i=0; i<photos.getLength(); i++) {
            Element photo = (Element) photos.item(i);

            String   farm = photo.getAttributeNode("farm").getValue();
            String server = photo.getAttributeNode("server").getValue();
            String     id = photo.getAttributeNode("id").getValue();
            String secret = photo.getAttributeNode("secret").getValue();

            String url = String.format("https://farm%s.static.flickr.com/%s/%s_%s_s.jpg", farm, server, id, secret);

            urls.add(url);
        }

        return urls;
    }

    public ImageView getImageFromUrl(String url) {
        ImageView imageView = new ImageView(this);
        Picasso.with(this)
               .load(url)
               .resize(75, 75)
               .into(imageView);
        return imageView;
    }
}
