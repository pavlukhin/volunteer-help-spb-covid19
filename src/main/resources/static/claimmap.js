claimsCtx = {};

initFilters();
ymaps.ready(init);

function init() {
    claimsCtx.map = new ymaps.Map("map", {
        center: [
            getLocalItem("mapCenter0", 59.919486),
            getLocalItem("mapCenter1", 30.442504)
        ],
        zoom: getLocalItem("mapZoom", "11"),
        controls: ["zoomControl"]
    });

    claimsCtx.map.events.add("boundschange", function(event) {
        localStorage.setItem("mapZoom", event.get("newZoom"));
        var newCenter = event.get("newCenter");
        localStorage.setItem("mapCenter0", newCenter[0]);
        localStorage.setItem("mapCenter1", newCenter[1]);
    });

    var goButton = new ymaps.control.Button({
        data: {
            content: "Поехали!"
        },
        options: {
            selectOnClick: false
        }
    });
    goButton.events.add("click", () => {
        var map0 = claimsCtx.map;
        var balloon = map0.balloon;
        var bData = balloon.getData();
        var gobj;
        if (bData) {
            if (bData.geoObject) {
                gobj = bData.geoObject;
            }
            else if (bData.cluster) {
                gobj = bData.cluster.state.get("activeObject");
            }
        }
        if (balloon.isOpen() && gobj) {
            var routeLink = buildRouteLink(gobj, map0.getCenter(), map0.getZoom());
            window.open(routeLink);
        }
        else {
            window.alert("Выберите заявку!");
        }
    });
    claimsCtx.map.controls.add(goButton);

    var xhr = new XMLHttpRequest();
    // t0d0 check lambdas safety
    xhr.onload = () => {
        // t0d0 reflect explicitly if there is no free claims
        var allClaims = JSON.parse(xhr.responseText);

        var openClaims0 = allClaims.filter(c => c.status === "OPEN");
        document.getElementById("openClaimsCbSuffix").textContent = "(" + openClaims0.length + ")";

        var openClaims = new ymaps.Clusterer({
            clusterDisableClickZoom: true,
            preset: "islands#greenClusterIcons"
        });
        openClaims.add(openClaims0.map(c => getClaimMark(c)));
        claimsCtx.openClaims = openClaims;

        var inProgressClaims0 = allClaims.filter(c => c.status === "IN_PROGRESS");
        document.getElementById("inProgressClaimsCbSuffix").textContent = "(" + inProgressClaims0.length + ")";

        var inProgressClaims = new ymaps.Clusterer({
            clusterDisableClickZoom: true,
            preset: "islands#orangeClusterIcons"
        });
        inProgressClaims.add(inProgressClaims0.map(c => getClaimMark(c)));
        claimsCtx.inProgressClaims = inProgressClaims;

        refreshMarks();
        document.getElementById("openClaimsCb").onclick = refreshMarks;
        document.getElementById("inProgressClaimsCb").onclick = refreshMarks;

        document.getElementById("loaderGlassPanel").style.display = "none";
    };
    xhr.open("GET", "/api/claims");
    xhr.send();
}

function getLocalItem(key, dflt) {
    var v = localStorage.getItem(key);
    return v == null ? dflt : v;
}

function initFilters() {
    var showOpen = getLocalItem("showOpenClaims", "1");
    var openClaimsCb = document.getElementById("openClaimsCb");
    if (showOpen === "1") {
        openClaimsCb.checked = true;
    }

    var showInProgress = getLocalItem("showInProgressClaims", "0");
    var inProgressClaimsCb = document.getElementById("inProgressClaimsCb");
    if (showInProgress === "1") {
        inProgressClaimsCb.checked = true;
    }
}

function refreshMarks() {
    localStorage.setItem("showOpenClaims", showOpenClaims() ? "1" : "0");
    localStorage.setItem("showInProgressClaims", showInProgressClaims() ? "1" : "0");
    claimsCtx.map.geoObjects.removeAll();
    if (showOpenClaims()) {
        claimsCtx.map.geoObjects.add(claimsCtx.openClaims);
    }
    if (showInProgressClaims()) {
        claimsCtx.map.geoObjects.add(claimsCtx.inProgressClaims);
    }
}

function showOpenClaims() {
    return document.getElementById("openClaimsCb").checked;
}

function showInProgressClaims() {
    return document.getElementById("inProgressClaimsCb").checked;
}

function getClaimMark(claim, activeMap) {
    // t0d0 handle invalid claim info properly
    var claimInfo = getClaimInfo(claim);
    if (claimInfo) {
      // t0d0 use Placemark?
      return new ymaps.GeoObject(
        feature = {
          geometry: {
            type: "Point",
            coordinates: claim.coord
//            coordinates: [59.919486, 30.442504]
          },
          properties: {
            iconCaption: claim.id,
            balloonContentHeader: claim.id,
            balloonContentBody: claimInfo.balloonText
          }
        },
        options = {
          iconColor: claimInfo.statusColor
        }
      );
    }
}

function getClaimInfo(claim) {
    var claimInfo = {};
    if (claim.status === "OPEN") {
        claimInfo.statusColor = "green"
        // t0d0 escape internal html?
        claimInfo.balloonText = claim.address + "<br>" + claim.details;
    }
    else if (claim.status === "IN_PROGRESS") {
        claimInfo.statusColor = "orange";
        // t0d0 escape internal html?
        claimInfo.balloonText = claim.address + "<br>" + claim.details + "<br>Выполняет " + claim.assignee;
    }
    else {
        console.log("Unknown claim status " + JSON.stringify(claim));
        return null;
    }
    return claimInfo;
}

function buildRouteLink(toObj, center, zoom) {
    var toCrd = toObj.geometry.getCoordinates();
    return "https://yandex.ru/maps/?z=" + zoom + "&ll=" + center[1] + "," + center[0] + "&l=map&rtext=~" + toCrd[0] + "," + toCrd[1] + "&origin=jsapi_2_1_76&from=api-maps";
}