import { Observable } from 'rxjs';
import * as Stomp from '@stomp/stompjs';

import {
  RUN_TRADE_STREAM,
  STOP_TRADE_STREAM,
  OFFER_TRADE,
} from './constants';

import { saveChartData, saveTradesData, saveWalletData } from './actions';

const url = 'ws://localhost:8080/stream';
const client = Stomp.client(url);
const socket$ = Observable.create((observer) => {
  client.connect({
  }, () => {
    client.subscribe('/stream', (message) => {
      observer.next(JSON.parse(message.body).payload);
    });
    client.subscribe('/user/stream', (message) => {
      observer.next(JSON.parse(message.body).payload);
    });
  }, (error) => {
    observer.error(new Error(error));
  });
});

const websocketTradesEpic = action$ => Observable.merge(
  action$.ofType(RUN_TRADE_STREAM)
    .mergeMap(() => socket$
      .retryWhen(e => e.zip(Observable.interval(1000)))
      .map((payload) => {
        switch (payload.type) {
          case 'TRADE':
            return saveTradesData(payload);
          case 'WALLET':
            return saveWalletData(payload);
          case 'PRICE':
          case 'AVG_PRICE':
            return saveChartData(payload);
          default:
            return payload;
        }
      })
      .takeUntil(action$.ofType(STOP_TRADE_STREAM))
      .catch(() => Observable.of({ type: 'ERROR' }))), // eslint-disable-line object-curly-newline
  action$.ofType(OFFER_TRADE)
    .mergeMap((msg) => {
      client.send('/trade/', {
      }, JSON.stringify(msg.payload));
      return Observable.empty();
    })
);

export default websocketTradesEpic;
