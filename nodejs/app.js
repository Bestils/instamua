var koa = require('koa');
var hbs = require('koa-hbs');
var bodyParser = require('koa-bodyparser');
var http = require('http');
var session = require('koa-generic-session');
var redisStore = require('koa-redis');
var logger = require('koa-logger');

var Config = require("./config")

//kao-router
var Router 		= require('koa-router');

var app = koa();

app.name = 'koa-instamua';

//some middlewares
app.use(bodyParser());
//using logger
app.use(logger())

//redis session store
app.use(session({
    store: redisStore({
        host: Config.Redis.host,
        port: Config.Redis.port,
        db: Config.Redis.db,
        pass: Config.Redis.pass
    })
}));

//static files
//TODO: move to nginx
app.use(require('koa-static')(__dirname + "/..", {maxAge: 3600000}));

// koa-hbs is middleware. `use` it before you want to render a view
app.use(hbs.middleware({
    viewPath: __dirname + '/views',
    defaultLayout: 'main',
    extname: '.hbs',
    partialsPath: __dirname + '/views/partials',
    layoutsPath: __dirname + '/views/layouts',
    disableCache: true //TODO: disable it in production enviroment
}));

//declare a router and adding routes
var router = new Router();

//main routes + handlers
app.use(router.middleware());

//listen for event
app.listen(3000);