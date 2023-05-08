import React from 'react';
// import renderer from 'react-test-renderer';
// import { render, screen } from '@testing-library/react';
import { render } from 'test/render';
import ClubNoneExistPage from 'pages/club/ClubNoneExistPage';

describe('<ClubNoneExistPage />', () => {
  it('shows the current count state.', () => {
    const component = render(<ClubNoneExistPage />);
    expect(component).toMatchSnapshot();
  });
});