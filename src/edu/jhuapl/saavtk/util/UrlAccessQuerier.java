package edu.jhuapl.saavtk.util;

import java.io.IOException;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class UrlAccessQuerier extends SwingWorker<Void, Void>
{
    public static final String PROGRESS_PROPERTY = "infoQueryProgress";
    public static final String DONE_PROPERTY = "infoQueryDone";
    public static final String CANCELED_PROPERTY = "infoQueryCanceled";

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

    public void query() throws IOException, InterruptedException
    {
        UrlState urlState = urlInfo.getState();

        if (forceUpdate || urlState.getStatus() == UrlStatus.UNKNOWN)
        {
            if (serverAccessEnabled)
            {
                try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlInfo, HttpRequestMethod.HEAD))
                {
                    urlInfo.update(closeableConnection.getConnection());
                }
            }
            else
            {
                urlInfo.update(UrlState.of(urlState.getUrl()));
            }
        }
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
        firePropertyChange(isCancelled() ? CANCELED_PROPERTY : DONE_PROPERTY, null, urlInfo.getState());
    }

}