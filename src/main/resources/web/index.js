/**
 * Created by syuchan on 2017/05/22.
 */
$(function () {
    open();
    $(".tab-content > .control > .button").on("click", function () {
        const $this = $(this);
        if (webSocket) {
            webSocket.send("btn-" + $this.attr("data-op"));
        }
    });
    $("#send").on("click", function () {
        var msg = "cmd-" + ($("#say").prop("checked") ? "say " : "") + $("#command").val();
        webSocket.send(msg);
    });
});

var webSocket;

function open() {
    console.log("call");
    webSocket = new WebSocket((location.protocol === "https:" ? "wss" : "ws") + "://" + location.host + "/sock");
    webSocket.onopen = function (e) {

    };
    webSocket.onmessage = function (e) {
        console.log(e.data);
        const body = JSON.parse(e.data);
        switch (body.type) {
            case "init":
                setRunning(body.running);
                const right = $(".content > .right");
                right.html("");
                body.players.forEach(function (val) {
                    addPlayer(val.name, val.ip);
                });
                break;
            case "add":
                switch (body.model) {
                    case "console":
                        addConsole(body.time, body.msg);
                        break;
                    case "player":
                        addPlayer(body.name, body.ip);
                        break;
                }
                break;
            case "remove":
                switch (body.model) {
                    case "player":
                        removePlayer(body.name);
                        break;
                }
                break;
            case "change":
                switch (body.model) {
                    case "running":
                        setRunning(body.status);
                        break;
                }
                break;
        }
    };
    webSocket.onclose = function (e) {
        webSocket = undefined;
        setTimeout(open, 3000);
    }
}

function setRunning(running) {
    const $tabbtn = $(".tab-content > .content > .button");
    if (running) {
        $tabbtn.addClass("active");
    } else {
        $tabbtn.removeClass("active");
    }
}

function addPlayer(name, ip) {
    $('<div class="data">' +
        '<canvas width="40" height="40" class="icon"></canvas>' +
        '<div><div class="name">' + name + '</div>' +
        '<div class="ip">' + ip + '</div></div>' +
        '</div>').appendTo(".content > .right");
}

function removePlayer(name) {
    $(".content > .right > .data > .name").filter(function (i, val) {
        const element = $(val);
        if (element.html() === name) element.parent().remove();
    })
}

function addConsole(time, msg) {
    $('<tr>' +
        '<th class="time">' + time + '</th>' +
        '<th>' + msg + '</th>' +
        '<tr>'
    ).appendTo(".content > .left > .table > .table-body > .body > tbody");
}