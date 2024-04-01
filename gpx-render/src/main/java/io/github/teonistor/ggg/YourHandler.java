package io.github.teonistor.ggg;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

public class YourHandler {

    public static final RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
            // Somewhere here is a pseudo-fingerprint which the GPSVisualiser backend uses to suss out automated scripts, but we counter-attack
            .defaultHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:101.0) Gecko/20100101 Firefox/101.0")
            .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .defaultHeader("Referer", "https://www.gpsvisualizer.com/map_input?form=leaflet");
//                .defaultHeader("Accept-Language", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")

    public String send(final InputStream inputStream) {

        final RestTemplate rest = restTemplateBuilder
                .defaultHeader("Content-Type", "multipart/form-data")
                .build();

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("format", "leaflet");
        body.add("width", "700");
        body.add("height", "auto");
        body.add("google_full_screen", "1");
        body.add("drawing_title", "");
        body.add("bg_map", "google_opentopomap");
        body.add("bg_opacity", "100");
        body.add("time_offset", "");
        body.add("units", "metric");
        body.add("google_api_key", "");
        body.add("add_elevation", "0");
        body.add("map_profile", "1");
        body.add("allow_export", "0");
        body.add("map_profile_visible", "0");
        body.add("map_profile_width", "");
        body.add("map_profile_filled", "1");
        body.add("zoom_control", "auto");
        body.add("google_scale", "1");
        body.add("google_utilities_menu", "1");
        body.add("google_maptype_control", "auto");
        body.add("google_opacity", "2");
        body.add("google_filter_map_types", "0");
        body.add("geolocation_control", "2");
        body.add("google_zoom_level", "");
        body.add("center_point", "");
        body.add("google_center_box", "1");
        body.add("google_mouse_box", "0");
        body.add("google_measurement", "both");
        body.add("google_searchbox", "0");
        body.add("google_street_view", "0");
        body.add("google_tilt", "0");
        body.add("legend_html", "");
        body.add("google_css", "");
        body.add("google_js", "");
        body.add("google_js2", "");
        body.add("thunderforest_api_key", "");
        body.add("ign_api_key", "");
        body.add("trk_opacity", "90");
        body.add("trk_width", "3");
        body.add("trk_colorize", "track");
        body.add("trk_hue", "0");
        body.add("trk_colorize_custom", "");
        body.add("vmg_point", "");
        body.add("colorize_min", "");
        body.add("colorize_max", "");
        body.add("lightness", "90");
        body.add("saturation", "100");
        body.add("colorize_reverse", "0");
        body.add("hue1", "0");
        body.add("hue2", "300");
        body.add("colorize_spectrum", "");
        body.add("colorize_gray", "1");
        body.add("legend_placement", "1");
        body.add("legend_steps", "5");
        body.add("legend_blocks", "1");
        body.add("trk_preserve_attr", "1");
        body.add("trk_list", "desc");
        body.add("tickmark_interval", "");
        body.add("tickmark_icon", "");
        body.add("cumulative_distance", "0");
        body.add("tickmark_zero", "0");
        body.add("trk_distance_threshold", "");
        body.add("trk_simplify", "");
        body.add("trk_as_wpt", "0");
        body.add("trk_as_wpt_name", "");
        body.add("trk_as_wpt_desc", "");
        body.add("show_trk", "1");
        body.add("reverse", "0");
        body.add("connect_segments", "0");
        body.add("trk_merge", "0");
        body.add("trk_segment_time", "");
        body.add("trk_reorder", "0");
        body.add("trk_reorder_threshold", "0");
        body.add("google_trk_outline", "0");
        body.add("google_trk_geodesic", "0");
        body.add("google_trk_clickable", "1");
        body.add("google_trk_mouseover", "0");
        body.add("trk_fill_opacity", "");
        body.add("moving_average", "1");
        body.add("convert_routes", "t_nw");
        body.add("trk_stats", "0");
        body.add("trk_elevation_gain", "0");
        body.add("trk_elevation_threshold", "");
        body.add("show_wpt", "3");
        body.add("padding", "10");
        body.add("google_wpt_sym", "pin");
        body.add("wpt_color", "red");
        body.add("wpt_sym_custom", "");
        body.add("google_wpt_labels", "0");
        body.add("google_wpt_label_text", "white");
        body.add("google_wpt_label_bg", "#333333");
        body.add("wpt_opacity", "100");
        body.add("wpt_scale", "1");
        body.add("google_wpt_shadows", "1");
        body.add("google_wpt_vectors", "1");
        body.add("wpt_preserve_attr", "1");
        body.add("wpt_colorize", "none");
        body.add("wpt_colorize_custom", "");
        body.add("synthesize_name", "");
        body.add("synthesize_desc", "");
        body.add("synthesize_label", "");
        body.add("synthesize_folder", "");
        body.add("marker_list_options:folders_collapsed", "0");
        body.add("wpt_list", "0");
        body.add("marker_list_options:width", "160");
        body.add("google_wpt_filter", "0");
        body.add("google_wpt_filter_limit", "");
        body.add("google_wpt_filter_list", "1");
        body.add("google_wpt_filter_sort", "0");
        body.add("discard_outliers", "0");
        body.add("wpt_name_filter", "");
        body.add("garmin_icons", "gpsmap");
        body.add("wpt_driving_directions", "0");
        body.add("wpt_interpolate", "1");
        body.add("wpt_interpolate_offset", "");

        // https://www.baeldung.com/spring-rest-template-multipart-upload
        // https://gist.github.com/stefan-huettemann/cc6025bf40208b67bd2d18d6d0599e69?permalink_comment_id=3445911#gistcomment-3445911

        final HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.add(CONTENT_DISPOSITION, "form-data; name=\"uploaded_file_1\"; filename=\"dummy.gpx\"");
        body.add("uploaded_file_1", new HttpEntity<>(new InputStreamResource(inputStream),
                fileHeaders));

        return rest.postForObject("https://www.gpsvisualizer.com/map?output_leaflet", body, String.class);
    }

//    public static void main(String[] arg) {
//        new YourHandler().a(new StringInputStream("aaaaaaaa"));
//    }
}
