package eu.nexabg.unischedule;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu.nexabg.unischedule.model.SDay;
import eu.nexabg.unischedule.model.SSubject;
import eu.nexabg.unischedule.model.Schedule;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView scheduleList;
    private SwipeRefreshLayout swipeRefresh;
    private AutoCompleteTextView weeksDropDown;

    private final List<Schedule> schedule = new ArrayList<>();
    private ScheduleListAdapter scheduleAdapter;

    public static List<String> items = new ArrayList<>();
    public static int selectedItem = -1;

    private Boolean autoRefresh = false;
    private Long lastPageDownload = 0L;
    private final int pageDownloadDelay = 600000; /* 10min */
    private Document pageHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setupList();
        refresh();

//        List<String> countries = Arrays.asList("USA", "Canada", "India", "Australia", "UK", "Germany", "France");
//        LineListAdapter adapter = new LineListAdapter(this, countries);
//        weeksDropDown.setAdapter(adapter);
    }

    private void init() {
        scheduleList = findViewById(R.id.scheduleList);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        weeksDropDown = findViewById(R.id.weeksDropDown);

        swipeRefresh.setOnRefreshListener(this::refresh);
        weeksDropDown.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedItem = i;
            lastPageDownload = 0L;
            refresh();
        });
    }

    private void refresh() {
        schedule.clear();

        Document body = readPage();

        // Loading schedule
        if (body != null) {
            Elements rows = body.select("tbody").get(1).select("tr");

            for (Element row : rows) {
                Elements cols = row.select("td");

                if (cols.size() == 1) {
                    Pattern pattern = Pattern.compile("([а-яА-Я]*)([, ]*)([0-9]*)-([0-9]*)-([0-9]*)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(cols.get(0).text());

                    if (matcher.matches()) {
                        String[] dateParts = cols.get(0).text().split(", ")[1].split("-");
                        Date date = new Date(Integer.parseInt(dateParts[0]) - 1900, Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));
                        schedule.add(new SDay(date));
                    }
                } else {
                    SSubject subject = new SSubject("", "", "", "");

                    List<String> strCols = cols.stream().map(Element::text).collect(Collectors.toList());
                    if (strCols.stream().filter(it -> !it.isBlank()).count() > 1) {
                        try {
                            Integer.parseInt(strCols.get(0));
                            subject.time = cols.get(1).text();
                            subject.location = cols.get(3).text();
                            subject.subject = cols.get(2).text();
                            subject.teacher = cols.get(4).text();
                            schedule.add(subject);
                        } catch (Exception e) {
                            System.err.println("Error");
                        }
                    }
                }
            }
        }

        scheduleAdapter.update();
        swipeRefresh.setRefreshing(false);
    }

    private void setupList() {
        scheduleAdapter = new ScheduleListAdapter(this, schedule);
        scheduleList.setHasFixedSize(true);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        scheduleList.setAdapter(scheduleAdapter);
    }

    private void setupOptions() {
        AtomicReference<Document> document = new AtomicReference<>();
        AtomicReference<Boolean> isDone = new AtomicReference<>(false);

        new Thread(() -> {
            try {
                document.set(Jsoup.connect("https://nvna.eu/wp/").get());
                pageHistory = document.get();
                lastPageDownload = System.currentTimeMillis();
            } catch (Exception e) {
                System.err.println("Document error");
            }
            isDone.set(true);
        }).start();

        while (!isDone.get()) {
        }

        Document body = document.get();
        if (body != null) {
            items.clear();
            Elements options = body.select("select[name=Week] option");

            for (int i = 0; i < options.size(); i++) {
                Element option = options.get(i);
                items.add(option.text());

                if (selectedItem == -1 && option.attr("selected").equals("selected")) {
                    selectedItem = i;
                }
                if (i == selectedItem) weeksDropDown.setText(option.text());
            }

            LineListAdapter adapter = new LineListAdapter(this, items);
            weeksDropDown.setAdapter(adapter);
            lastPageDownload = 0L;
        }
    }

    private Document readPage() {
        if (System.currentTimeMillis() - lastPageDownload < pageDownloadDelay) {
            return pageHistory;
        }
        swipeRefresh.setRefreshing(true);

        AtomicReference<Document> document = new AtomicReference<>();
        AtomicReference<Boolean> isDone = new AtomicReference<>(false);

        new Thread(() -> {
            try {
                document.set(Jsoup.connect(String.format("https://nvna.eu/wp/?group=140241&queryType=group&Week=%s", selectedItem + 1)).get());
                pageHistory = document.get();
                lastPageDownload = System.currentTimeMillis();
            } catch (Exception e) {
                System.err.println("Document error");
            }
            isDone.set(true);
        }).start();

        while (!isDone.get()) {
        }
        return document.get();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupOptions();
        refresh();

        autoRefresh = true;
        new Thread(() -> {
            while (autoRefresh) {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(this::refresh);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        autoRefresh = false;
    }
}