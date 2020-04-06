ymaps.ready(init);

function init() {
    var activeMap = new ymaps.Map("map", {
        center: [59.919486, 30.442504],
        zoom: 11
    });

    var xhr = new XMLHttpRequest();
    // t0d0 check lambdas safety
    xhr.onload = () => {
        // t0d0 reflect explicitly if there is no free claims
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
      // t0d0 handle non-single and failed results gracefully
      var target = qres._objects[0];
      // t0d0 escape internal html?
      var claimInfoStr = claim.address + "<br>" + claim.details;
      var gobj = new ymaps.GeoObject(
        feature = {
          geometry: {
            type: "Point",
            coordinates: target.geometry._coordinates
            //coordinates: [59.919486, 30.442504]
          },
          properties: {
            iconCaption: claim.id,
            balloonContentHeader: claim.id,
            balloonContentBody: claimInfoStr
          }
        },
        options = {
          iconColor: "green"
        }
      );
      activeMap.geoObjects.add(gobj);
    });
}