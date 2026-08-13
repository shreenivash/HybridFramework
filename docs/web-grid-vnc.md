# Web, Selenium Grid, and VNC

Grid sessions are **headed by default** so you can watch them. Set `-Dheadless=true` only when you do not need VNC.

## Start Grid

```bash
docker compose up -d
```

Open the Grid console: [http://localhost:4444](http://localhost:4444).

Each browser node allows **one session** (`SE_NODE_MAX_SESSIONS=1`) so the VNC desktop shows a single browser, not overlapping windows.

## Connect a VNC viewer

| Node | VNC Viewer (port 5900 in the container) | Browser noVNC |
|---|---|---|
| chrome-1 | `localhost:5900` | [http://localhost:7900](http://localhost:7900) |
| chrome-2 | `localhost:5901` | [http://localhost:7901](http://localhost:7901) |
| firefox-1 | `localhost:5902` | [http://localhost:7902](http://localhost:7902) |

Examples:

- RealVNC / TigerVNC: host `localhost`, port `5900`
- macOS Screen Sharing: `vnc://localhost:5900`
- noVNC in a browser: `http://localhost:7900`

Password is disabled (`SE_VNC_NO_PASSWORD=1`). Override with `SE_VNC_PASSWORD` in Compose if the Grid is not on localhost.

Do **not** `docker compose up --scale chrome=N` for VNC: host ports must stay unique. Add another named service (copy `chrome-2`) instead.

## Run web examples

```bash
mvn -pl examples test \
  -DsuiteXmlFile=src/test/resources/testng-web.xml \
  -Dgrid.url=http://localhost:4444 \
  -Dheadless=false
```

`BrowserFactory` uses `RemoteWebDriver` whenever `grid.url` is set, and sends `se:screenResolution=1920x1080`.
