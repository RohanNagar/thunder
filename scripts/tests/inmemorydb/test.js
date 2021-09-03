import { fullTest } from '../common/common.js';

export const options = {
  thresholds: {
    checks: [{
      threshold:   'rate == 1.00',
      abortOnFail: true
    }]
  },
  vus:        1,
  iterations: 1
};

export default function testSuite() {
  fullTest('inmemorydb');
}
