package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;

public abstract class UrlAccessQuerier extends SwingWorker<Void, Void>
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static final String QUERY_PROGRESS = "queryProgress";
    public static final String QUERY_DONE = "queryDone";
    public static final String QUERY_CANCELED = "queryCanceled";

    public static UrlAccessQuerier of(UrlInfo urlInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        Preconditions.checkNotNull(urlInfo);

        return new UrlAccessQuerier(urlInfo, serverAccessEnabled) {

            @Override
            public boolean isForceUpdate()
            {
                return forceUpdate;
            }

        };
    }

    private final UrlInfo urlInfo;
    private final boolean serverAccessEnabled;

    protected UrlAccessQuerier(UrlInfo urlInfo, boolean serverAccessEnabled)
    {
        this.urlInfo = urlInfo;
        this.serverAccessEnabled = serverAccessEnabled;
    }

    public UrlState getUrlState()
    {
        return urlInfo.getState();
    }

    public abstract boolean isForceUpdate();

    public void query() throws IOException
    {
        UrlState urlState = getUrlState();
        UrlStatus status = urlState.getLastKnownStatus();

        boolean needsUpdate = status == UrlStatus.UNKNOWN || status == UrlStatus.CONNECTION_ERROR || status == UrlStatus.HTTP_ERROR;

        if (isForceUpdate() || needsUpdate)
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
        else
        {
            urlInfo.update(urlState.update(serverAccessEnabled));
        }
    }

    protected void queryServer() throws IOException
    {
        UrlState urlState = getUrlState();

        if (serverAccessEnabled)
        {
            try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlState.getUrl(), HttpRequestMethod.HEAD))
            {
                urlInfo.update(closeableConnection);
            }
        }
        else
        {
            // Do not update the URL info other than to report that the server access is
            // disabled.
            urlInfo.update(urlState.update(false));
        }
    }

    protected void queryFileSystem() throws IOException
    {
        UrlState urlState = getUrlState();

        URL url = urlState.getUrl();

        File file = new File(url.getPath());

        if (file.exists())
        {
            urlState = UrlState.of(url, UrlStatus.ACCESSIBLE, file.length(), file.lastModified(), true);
        }
        else
        {
            urlState = UrlState.of(url, UrlStatus.NOT_FOUND, true);
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