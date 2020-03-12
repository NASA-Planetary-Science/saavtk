package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;

public class UrlAccessQuerier extends SwingWorker<Void, Void>
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static final String QUERY_PROGRESS = "queryProgress";
    public static final String QUERY_DONE = "queryDone";
    public static final String QUERY_CANCELED = "queryCanceled";

    public static UrlAccessQuerier of(UrlInfo urlInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        Preconditions.checkNotNull(urlInfo);

        return new UrlAccessQuerier(urlInfo, forceUpdate, serverAccessEnabled);
    }

    private final UrlInfo urlInfo;
    private final boolean forceUpdate;
    private final boolean serverAccessEnabled;

    protected UrlAccessQuerier(UrlInfo urlInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        this.urlInfo = urlInfo;
        this.forceUpdate = forceUpdate;
        this.serverAccessEnabled = serverAccessEnabled;
    }

    public UrlState getUrlState()
    {
        return urlInfo.getState();
    }

    public boolean isForceUpdate()
    {
        return forceUpdate;
    }

    public void query() throws IOException
    {
        UrlState urlState = urlInfo.getState();

        if (forceUpdate || urlState.getStatus() == UrlStatus.UNKNOWN || urlState.getStatus() == UrlStatus.CONNECTION_ERROR)
        {
            if (SAFE_URL_PATHS.hasFileProtocol(urlState.getUrl().toString()))
            {
                queryFileSystem();
            }
            else
            {
                queryServer();
            }
        }
    }

    protected void queryServer() throws IOException
    {
        UrlState urlState = urlInfo.getState();

        if (serverAccessEnabled)
        {
            try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlState.getUrl(), HttpRequestMethod.HEAD))
            {
                urlInfo.update(closeableConnection.getConnection());
            }
        }
        else
        {
            // This resets the state to a new/unknown condition.
            urlInfo.update(UrlState.of(urlState.getUrl()));
        }
    }

    protected void queryFileSystem() throws IOException
    {
        UrlState urlState = urlInfo.getState();

        URL url = urlState.getUrl();

        File file = new File(url.getPath());

        if (file.exists())
        {
            urlState = UrlState.of(url, UrlStatus.ACCESSIBLE, file.length(), file.lastModified());
        }
        else
        {
            urlState = UrlState.of(url, UrlStatus.NOT_FOUND);
        }

        urlInfo.update(urlState);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        query();

        return null;
    }

    @Override
    public void done()
    {
        firePropertyChange(isCancelled() ? QUERY_CANCELED : QUERY_DONE, null, urlInfo.getState());
    }

}