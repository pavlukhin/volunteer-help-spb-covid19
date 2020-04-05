ymaps.ready(init);

function init() {
    var activeMap = new ymaps.Map("map", {
        center: [59.919486, 30.442504],
        zoom: 13
    });

    var xhr = new XMLHttpRequest();
    xhr.onload = () => {
        var claims = JSON.parse(xhr.responseText);
        claims.forEach(claim => {
            putClaimMark(claim, activeMap);
        });
    };
    xhr.open("GET", "/api/claims");
    xhr.send();
}

function putClaimMark(claim, activeMap) {
    var qres = ymaps.geoQuery(ymaps.geocode(claim.address));
    qres.then(function() {
      var target = qres._objects[0];
      var gobj = new ymaps.GeoObject(
        feature = {
          geometry: {
            type: "Point",
            coordinates: target.geometry._coordinates
            //coordinates: [59.919486, 30.442504]
          },
          properties: {iconCaption: claim.id}
          //properties: target.properties
        },
        options = {
          iconColor: "green"
        }
      );
      activeMap.geoObjects.add(gobj);
    });
}